package com.bobocode.entity;

import com.bobocode.annotation.Column;
import com.bobocode.annotation.Entity;
import com.bobocode.annotation.Id;
import com.bobocode.annotation.Table;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "person")
@NoArgsConstructor
@ToString
public class Person {
    @Id
    private Long id;

    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}