
package com.sparta.scheduler.dto;

import lombok.Getter;

import java.sql.Date;

@Getter
public class SchedulerRequestDto {
    private String title;
    private String contents;
    private String username;
    private String password;
    private Date date;
}