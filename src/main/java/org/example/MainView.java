package org.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("") // Route annotation to define the URL path for this view
public class MainView extends VerticalLayout {
    private final Semaphore semaphore = new Semaphore(5); // A Semaphore to limit the number of concurrent tasks to 5
    private int taskCounter = 0; // A counter to keep track of the number of tasks created
    private int finishedTasks = 0; // A counter to keep track of the number of tasks that have finished
    private final List<Task> tasks = new ArrayList<>();
    private final Grid<Task> taskGrid = new Grid<>();
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);

    public MainView() {
        // Initialize the grid
        taskGrid.addColumn(Task::getId).setHeader("ID");
        taskGrid.addColumn(Task::getStatus).setHeader("Status");
        taskGrid.setItems(tasks);

        // Create a button that will start a background task when clicked
        Button backgroundTaskButton = new Button("Spustit úlohu na pozadí", event -> {
            // Get the current UI
            UI ui = UI.getCurrent();
            taskCounter++;
            Task task;

            // Automatic failure of every 5th task run
            if (taskCounter % 5 == 0) {
                task = new Task(taskCounter, "Selhala");
                ui.accessSynchronously(() -> {
                    Notification.show("Úloha " + task.getId() + ": Selhala.");
                    tasks.add(task);
                    taskGrid.getDataProvider().refreshAll();
                    logger.error("Úloha č." + task.getId() + " selhala!");
                });
            } else {
                // Create a new Task object with the current task number
                task = new Task(taskCounter, "");
                tasks.add(task);
                taskGrid.getDataProvider().refreshAll();
                if (taskCounter - finishedTasks > 5) {
                    // If there are already 5 tasks running, show a notification on the UI thread that the task has been queued
                    ui.accessSynchronously(() -> {
                        Notification.show("Úloha " + task.getId() + ": Zařazena do fronty.");
                        task.setStatus("Zařazena do fronty");
                        taskGrid.getDataProvider().refreshItem(task);
                        logger.info("Úloha č." + task.getId() + " zařazena do fronty.");
                    });
                }
            }
            // Launch the task in a new thread
            if (!"Selhala".equals(task.getStatus())) {
                new Thread(() -> {
                    try {
                        // Acquire the semaphore before starting the background task
                        try {
                            semaphore.acquire();
                            // Show notification on the UI thread that the task has started running
                            ui.accessSynchronously(() -> {
                                Notification.show("Úloha " + task.getId() + ": Běží.");
                                task.setStatus("Běží");
                                taskGrid.getDataProvider().refreshItem(task);
                            });
                            logger.info("Úloha č." + task.getId() + " běží.");
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        // Random task length
                        Thread.sleep((long) (Math.random() * 5000 + 5000));
                        // Collect data using REST API
                        try {
                            URL url = new URL("http://ip.jsontest.com/");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("Accept", "application/json");

                            BufferedReader br = new BufferedReader(new InputStreamReader(
                                    (conn.getInputStream())));

                            String output;
                            while ((output = br.readLine()) != null) {
                                // Parse the JSON response to get the IP address
                                if (output.contains("ip")) {
                                    String ipAddress = output.split(":")[1].replaceAll("\"", "").replaceAll(",", "").replaceAll("}", "").trim();
                                    logger.info("IP Address: " + ipAddress);
                                }
                            }
                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            // Release the semaphore after the task is finished
                            semaphore.release();
                            finishedTasks++;
                            // Show notification on the UI thread
                            ui.accessSynchronously(() -> {
                                Notification.show("Úloha " + task.getId() + ": Skončila.");
                                task.setStatus("Skončila");
                                taskGrid.getDataProvider().refreshItem(task);
                                tasks.remove(task);
                                taskGrid.getDataProvider().refreshAll();
                            });
                            logger.info("Úloha č." + task.getId() + " dokončena.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
        add(backgroundTaskButton, taskGrid);
    }
}

