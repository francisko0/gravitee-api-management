/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.apim.core.specgen.usecase;

import static assertions.CoreAssertions.assertThat;
import static io.gravitee.apim.core.specgen.model.ApiSpecGenRequestState.AVAILABLE;
import static io.gravitee.apim.core.specgen.model.ApiSpecGenRequestState.GENERATING;
import static io.gravitee.apim.core.specgen.model.ApiSpecGenRequestState.STARTED;
import static io.gravitee.apim.core.specgen.model.ApiSpecGenRequestState.UNAVAILABLE;
import static io.gravitee.rest.api.service.common.UuidString.generateRandom;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;

import inmemory.ApiSpecGenCrudServiceInMemory;
import inmemory.ApiSpecGenQueryServiceInMemory;
import inmemory.SpecGenProviderInMemory;
import io.gravitee.apim.core.specgen.model.ApiSpecGen;
import io.gravitee.apim.core.specgen.model.ApiSpecGenRequestReply;
import io.gravitee.apim.core.specgen.model.ApiSpecGenRequestState;
import io.gravitee.apim.core.specgen.use_case.SpecGenRequestUseCase;
import io.gravitee.definition.model.v4.ApiType;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SpecGenRequestUseCaseTest {

    static final ApiSpecGenQueryServiceInMemory queryService = new ApiSpecGenQueryServiceInMemory();
    static final SpecGenProviderInMemory specGenProvider = new SpecGenProviderInMemory();
    static final ApiSpecGenCrudServiceInMemory crudService = new ApiSpecGenCrudServiceInMemory();

    private SpecGenRequestUseCase useCase;
    private static final String CURRENT_ENV = "envId";
    private static final String API_ID = generateRandom();

    @BeforeEach
    void setUp() {
        useCase = new SpecGenRequestUseCase(queryService, specGenProvider, crudService);

        GraviteeContext.setCurrentEnvironment(CURRENT_ENV);
        crudService.initWith(List.of());
        queryService.initWith(
            List.of(
                new ApiSpecGen(generateRandom(), "api-1", "some description", "some-version", ApiType.MESSAGE, CURRENT_ENV),
                new ApiSpecGen(generateRandom(), "api-2", "some description", "some-version", ApiType.NATIVE, CURRENT_ENV),
                new ApiSpecGen(API_ID, "api-3", "some description", "some-version", ApiType.PROXY, "otherEnvId"),
                new ApiSpecGen(API_ID, "api-4", "some description", "some-version", ApiType.PROXY, CURRENT_ENV)
            )
        );
    }

    @Test
    void must_return_unavailable_get_state_with_absent_api() {
        queryService.initWith(List.of());

        useCase
            .getState(API_ID, generateRandom())
            .test()
            .awaitDone(2, SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(state -> UNAVAILABLE.equals(state.state()));
    }

    @Test
    void must_return_unavailable_post_job_with_absent_api() {
        queryService.initWith(List.of());
        useCase
            .postJob(API_ID, generateRandom())
            .test()
            .awaitDone(2, SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(state -> UNAVAILABLE.equals(state.state()));
    }

    static Stream<Arguments> params_that_must_return_post_job_request_state() {
        return Stream.of(Arguments.of(UNAVAILABLE, 0), Arguments.of(STARTED, 1), Arguments.of(GENERATING, 0));
    }

    @ParameterizedTest
    @MethodSource("params_that_must_return_post_job_request_state")
    void must_return_post_job_request_state(ApiSpecGenRequestState state, int crudSize) {
        specGenProvider.initWith(List.of(Single.just(new ApiSpecGenRequestReply(state))));

        useCase
            .postJob(API_ID, generateRandom())
            .test()
            .awaitDone(2, SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(reply -> state.equals(reply.state()));

        assertThat(crudService.storage()).hasSize(crudSize);
    }

    public static Stream<Arguments> params_that_must_return_get_state_request_state() {
        return Stream.of(Arguments.of(AVAILABLE, 0), Arguments.of(UNAVAILABLE, 0), Arguments.of(GENERATING, 0));
    }

    @ParameterizedTest
    @MethodSource("params_that_must_return_get_state_request_state")
    void must_return_get_state_request_state(ApiSpecGenRequestState state, int crudSize) {
        specGenProvider.initWith(List.of(Single.just(new ApiSpecGenRequestReply(state))));

        useCase
            .getState(API_ID, generateRandom())
            .test()
            .awaitDone(2, SECONDS)
            .assertComplete()
            .assertNoErrors()
            .assertValue(reply -> state.equals(reply.state()));

        assertThat(crudService.storage()).hasSize(crudSize);
    }

    @AfterEach
    void tearDown() {
        GraviteeContext.cleanContext();
    }
}
