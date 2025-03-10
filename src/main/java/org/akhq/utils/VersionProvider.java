package org.akhq.utils;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertiesPropertySourceLoader;
import io.micronaut.core.util.StringUtils;
import lombok.Getter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class VersionProvider {
    @Getter
    private String version = "Snapshot";

    @Inject
    Environment environment;

    @PostConstruct
    public void start() {
        this.version = Stream
            .concat(
                new PropertiesPropertySourceLoader()
                    .load("classpath:gradle", environment)
                    .stream()
                    .flatMap(properties -> Stream.of(properties.get("version"))),
                new PropertiesPropertySourceLoader()
                    .load("classpath:git", environment)
                    .stream()
                    .flatMap(properties -> Stream
                        .of(
                            properties.get("git.tags"),
                            properties.get("git.branch")
                        )
                    )
            )
            .map(this::getVersion)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(this.version);
    }

    private Optional<String> getVersion(Object object) {
        String candidate = Objects.toString(object, null);

        return StringUtils.isNotEmpty(candidate) ? Optional.of(candidate) : Optional.empty();
    }
}
