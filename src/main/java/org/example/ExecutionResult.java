package org.example;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ExecutionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exec_datetime", nullable = false)
    private LocalDateTime execDateTime;

    @Column(name = "run_sequence", nullable = false)
    private Integer runSequence;

    @Column(name = "run_succeeded", nullable = false)
    private Boolean runSucceeded;

    @Column(name = "error_text")
    private String errorText;

    public ExecutionResult(){

    }

    public ExecutionResult(LocalDateTime execDateTime, Integer runSequence, Boolean runSucceeded, String errorText) {
        this.execDateTime = execDateTime;
        this.runSequence = runSequence;
        this.runSucceeded = runSucceeded;
        this.errorText = errorText;
    }

    public LocalDateTime getExecDateTime() {
        return execDateTime;
    }

    public void setExecDateTime(LocalDateTime execDateTime) {
        this.execDateTime = execDateTime;
    }

    public Integer getRunSequence() {
        return runSequence;
    }

    public void setRunSequence(Integer runSequence) {
        this.runSequence = runSequence;
    }

    public Boolean getRunSucceeded() {
        return runSucceeded;
    }

    public void setRunSucceeded(Boolean runSucceeded) {
        this.runSucceeded = runSucceeded;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
