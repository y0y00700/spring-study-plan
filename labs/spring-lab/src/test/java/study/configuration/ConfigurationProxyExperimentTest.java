package study.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConfigurationProxyExperimentTest {

    private static int memberConstructorCalls;

    @BeforeEach
    void resetConstructorCalls() {
        memberConstructorCalls = 0;
    }

    static class Member {

        Member() {
            memberConstructorCalls++;
        }
    }

    static class Team {

        private final Member member;

        Team(Member member) {
            this.member = member;
        }

        Member getMember() {
            return member;
        }
    }

    @Configuration(proxyBeanMethods = true)
    static class ProxiedConfig {

        @Bean
        Member member() {
            return new Member();
        }

        @Bean
        Team team() {
            return new Team(member());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class PlainConfig {

        @Bean
        Member member() {
            return new Member();
        }

        @Bean
        Team team() {
            return new Team(member());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ParameterConfig {

        @Bean
        Member member() {
            return new Member();
        }

        @Bean
        Team team(Member member) {
            return new Team(member);
        }
    }

    @Test
    void proxyBeanMethodsTrueUsesManagedBean() {
        try (var context =
                     new AnnotationConfigApplicationContext(ProxiedConfig.class)) {
            Member contextMember = context.getBean(Member.class);
            Team team = context.getBean(Team.class);

            assertSame(contextMember, team.getMember());
            assertEquals(1, memberConstructorCalls);
        }
    }

    @Test
    void proxyBeanMethodsFalseUsesDirectMethodCall() {
        try (var context =
                     new AnnotationConfigApplicationContext(PlainConfig.class)) {
            Member contextMember = context.getBean(Member.class);
            Team team = context.getBean(Team.class);

            assertNotSame(contextMember, team.getMember());
            assertEquals(2, memberConstructorCalls);
        }
    }

    @Test
    void proxyBeanMethodsFalseCanUseMethodParameterInjection() {
        try (var context =
                     new AnnotationConfigApplicationContext(ParameterConfig.class)) {
            Member contextMember = context.getBean(Member.class);
            Team team = context.getBean(Team.class);

            assertSame(contextMember, team.getMember());
            assertEquals(1, memberConstructorCalls);
        }
    }
}
