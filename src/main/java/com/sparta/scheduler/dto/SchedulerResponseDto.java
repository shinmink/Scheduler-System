
package com.sparta.scheduler.dto;

import com.sparta.scheduler.entity.Scheduler;
import lombok.Getter;

import java.sql.Date;

@Getter
public class SchedulerResponseDto {
    private Long id;
    private String title;
    private String contents;
    private String username;
    private String password;
    private Date date;

    public SchedulerResponseDto(Scheduler scheduler) {
        this.id = scheduler.getId();
        this.title = scheduler.getTitle();
        this.contents = scheduler.getContents();
        this.username = scheduler.getUsername();
        this.password = scheduler.getPassword();
        this.date = scheduler.getDate();
    }

    public SchedulerResponseDto(Long id, String title, String contents, String username, String password, Date date ) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.username = username;
        this.password = password;
        this.date = date;


    }
}