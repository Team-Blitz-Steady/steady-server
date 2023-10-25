package dev.steady.review.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stcikers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Builder
    private Sticker(String content) {
        this.content = content;
    }

}