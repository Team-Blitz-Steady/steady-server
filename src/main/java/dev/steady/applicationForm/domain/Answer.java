package dev.steady.applicationForm.domain;


import dev.steady.steadyForm.domain.Question;
import dev.steady.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "content", nullable = false)
    private String content;

    @Builder
    private Answer(Question question, User user, String content) {
        this.question = question;
        this.user = user;
        this.content = content;
    }

}
