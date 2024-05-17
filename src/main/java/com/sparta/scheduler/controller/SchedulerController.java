package com.sparta.scheduler.controller;

import com.sparta.scheduler.dto.SchedulerRequestDto;
import com.sparta.scheduler.dto.SchedulerResponseDto;
import com.sparta.scheduler.entity.Scheduler;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SchedulerController {

    private final JdbcTemplate jdbcTemplate;

    public SchedulerController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/schedulers")
    public SchedulerResponseDto createScheduler(@RequestBody SchedulerRequestDto requestDto) {
        // RequestDto -> Entity
        Scheduler scheduler = new Scheduler(requestDto);

        // DB 저장
        KeyHolder keyHolder = new GeneratedKeyHolder(); // 기본 키를 반환받기 위한 객체

        String sql = "INSERT INTO scheduler (title, contents, username, password, date) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(con -> {
                    PreparedStatement preparedStatement = con.prepareStatement(sql,
                            Statement.RETURN_GENERATED_KEYS);

                    preparedStatement.setString(1, scheduler.getTitle());
                    preparedStatement.setString(2, scheduler.getContents());
                    preparedStatement.setString(3, scheduler.getUsername());
                    preparedStatement.setString(4, scheduler.getPassword());
                    preparedStatement.setDate(5, scheduler.getDate());

                    return preparedStatement;
                },
                keyHolder);

        // DB Insert 후 받아온 기본키 확인
        Long id = keyHolder.getKey().longValue();
        scheduler.setId(id);

        // Entity -> ResponseDto
        SchedulerResponseDto schedulerResponseDto = new SchedulerResponseDto(scheduler);

        return schedulerResponseDto;
    }

    @GetMapping("/schedulers")
    public List<SchedulerResponseDto> getschedulers() {
        // DB 조회
        String sql = "SELECT * FROM scheduler";

        return jdbcTemplate.query(sql, new RowMapper<SchedulerResponseDto>() {
            @Override
            public SchedulerResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                // SQL 의 결과로 받아온 Schedulers 데이터들을 SchedulersResponseDto 타입으로 변환해줄 메서드
                Long id = rs.getLong("id");
                String title = rs.getString("title");
                String contents = rs.getString("contents");
                String username = rs.getString("username");
                String password = rs.getString("password");
                Date date = rs.getDate("date");
                return new SchedulerResponseDto(id, title, contents, username, password, date);
            }
        });
    }

    @PutMapping("/schedulers/{id}")
    public Long updateScheduler(@PathVariable Long id, @RequestBody SchedulerRequestDto requestDto) {
        // 해당 메모가 DB에 존재하는지 확인
        Scheduler scheduler = findById(id);
        if (scheduler != null) {
            // 비밀번호 확인
            if (!scheduler.getPassword().equals(requestDto.getPassword())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비밀번호가 올바르지 않습니다.");
            }

            // Null 및 빈 값 검사 추가
            validateRequestDto(requestDto);

            // scheduler 내용 수정
            String sql = "UPDATE scheduler SET title = ?, contents = ?, username = ?, password = ?, date = ? WHERE id = ?";

            jdbcTemplate.update(sql, requestDto.getTitle(), requestDto.getContents(),
                    requestDto.getUsername(), requestDto.getPassword(), requestDto.getDate(), id);

            return id;
        } else {
            throw new IllegalArgumentException("선택한 일정은 존재하지 않습니다.");
        }
    }

    private void validateRequestDto(SchedulerRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (requestDto.getContents() == null || requestDto.getContents().isEmpty()) {
            throw new IllegalArgumentException("Contents cannot be null or empty");
        }
        if (requestDto.getUsername() == null || requestDto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (requestDto.getPassword() == null || requestDto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (requestDto.getDate() == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }

    @DeleteMapping("/schedulers/{id}")
    public Long deleteScheduler(@PathVariable Long id, @RequestBody SchedulerRequestDto requestDto) {
        // 해당 메모가 DB에 존재하는지 확인
        Scheduler scheduler = findById(id);
        if (scheduler != null) {
            // 비밀번호 확인
            if (!scheduler.getPassword().equals(requestDto.getPassword())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비밀번호가 올바르지 않습니다.");
            }

            // scheduler 삭제
            String sql = "DELETE FROM scheduler WHERE id = ?";
            jdbcTemplate.update(sql, id);

            return id;
        } else {
            throw new IllegalArgumentException("선택한 일정은 존재하지 않습니다.");
        }
    }

    private Scheduler findById(Long id) {
        // DB 조회
        String sql = "SELECT * FROM scheduler WHERE id = ?";

        return jdbcTemplate.query(sql, resultSet -> {
            if (resultSet.next()) {
                Scheduler scheduler = new Scheduler();
                scheduler.setTitle(resultSet.getString("title"));
                scheduler.setContents(resultSet.getString("contents"));
                scheduler.setUsername(resultSet.getString("username"));
                scheduler.setPassword(resultSet.getString("password"));
                scheduler.setDate(resultSet.getDate("date"));
                return scheduler;
            } else {
                return null;
            }
        }, id);
    }
}
