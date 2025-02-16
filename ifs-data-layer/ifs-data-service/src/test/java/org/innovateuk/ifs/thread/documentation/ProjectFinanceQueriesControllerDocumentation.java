package org.innovateuk.ifs.thread.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.documentation.PostResourceDocs;
import org.innovateuk.ifs.documentation.UserDocs;
import org.innovateuk.ifs.project.queries.controller.ProjectFinanceQueriesController;
import org.innovateuk.ifs.project.queries.transactional.FinanceCheckQueriesService;
import org.innovateuk.ifs.threads.resource.FinanceChecksSectionType;
import org.innovateuk.ifs.threads.resource.PostResource;
import org.innovateuk.ifs.threads.resource.QueryResource;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.List;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.QueryFieldsDocs.queryResourceFields;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectFinanceQueriesControllerDocumentation extends BaseControllerMockMVCTest<ProjectFinanceQueriesController> {

    @Mock
    private FinanceCheckQueriesService financeCheckQueriesService;

    @Test
    public void findOne() throws Exception {
        final Long queryId = 3L;
        List<PostResource> posts = asList(new PostResource(97L, newUserResource().withId(7L).build(), "Post message", asList(), now()));
        final QueryResource query = new QueryResource(3L, 22L, posts, FinanceChecksSectionType.VIABILITY, "New query title", true, now(), null, null);
        when(financeCheckQueriesService.findOne(queryId)).thenReturn(serviceSuccess(query));

        mockMvc.perform(get("/project/finance/queries/{queryId}", queryId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(query)))
                .andDo(document("project/finance/queries/{method-name}",
                        pathParameters(parameterWithName("queryId").description("Id of the Query to be fetched")),
                        responseFields(queryResourceFields())
                                .andWithPrefix("posts[].", PostResourceDocs.postResourceFields)
                                .andWithPrefix("posts[].author.", UserDocs.userResourceFields)));
    }

    @Test
    public void findAll() throws Exception {
        final Long contextId = 22L;
        List<PostResource> posts = asList(new PostResource(33L, newUserResource().withId(7L).build(), "Post message", asList(), now()));
        final QueryResource query = new QueryResource(3L, 22L, posts, FinanceChecksSectionType.VIABILITY, "New query title", true, now(), null, null);
        when(financeCheckQueriesService.findAll(contextId)).thenReturn(serviceSuccess(asList(query)));

        mockMvc.perform(get("/project/finance/queries/all/{projectFinanceId}", contextId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(asList(query))))
                .andDo(document("project/finance/queries/{method-name}",
                        responseFields(fieldWithPath("[]").description("List of Queries the authenticated user has access to"))
                        .andWithPrefix("[].", queryResourceFields())
                        .andWithPrefix("[].posts[].", PostResourceDocs.postResourceFields)
                        .andWithPrefix("[].posts[].author.", UserDocs.userResourceFields),
                        pathParameters(parameterWithName("projectFinanceId").description("The id of the project finance under which the expected queries live."))));
    }


    @Test
    public void addPost() throws Exception {
        Long queryId = 22L;
        PostResource post = new PostResource(null, newUserResource().withId(7L).build(), "Post message", asList(), null);
        when(financeCheckQueriesService.addPost(post, queryId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/finance/queries/{queryId}/post", queryId)
                .header("IFS_AUTH_TOKEN", "123abc")
                .contentType(APPLICATION_JSON)
                .content(toJson(post)))
                .andExpect(status().isCreated())
                .andDo(document("project/finance/queries/{method-name}",
                        pathParameters(parameterWithName("queryId").description("Id of the Query to which the Post is to be added to."))));
    }

    @Test
    public void create() throws Exception {
        List<PostResource> posts = asList(new PostResource(null, newUserResource().withId(7L).build(), "Post message", asList(), null));
        final QueryResource query = new QueryResource(null, 22L, posts, FinanceChecksSectionType.VIABILITY, "New query title", false, null, null, null);

        when(financeCheckQueriesService.create(query)).thenReturn(serviceSuccess(55L));

        mockMvc.perform(post("/project/finance/queries")
                .header("IFS_AUTH_TOKEN", "123abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(query)))
                .andExpect(content().string(objectMapper.writeValueAsString(55L)))
                .andExpect(status().isCreated())
                .andDo(document("project/finance/queries/{method-name}",
                        requestFields(queryResourceFields())
                                .andWithPrefix("posts[].", PostResourceDocs.postResourceFields)
                                .andWithPrefix("posts[].author.", UserDocs.userResourceFields)));
    }

    @Test
    public void close() throws Exception {

        Long threadId = 1L;
        when(financeCheckQueriesService.close(threadId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/finance/queries/thread/{threadId}/close", threadId)
                .header("IFS_AUTH_TOKEN", "123abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("project/finance/queries/{method-name}",
                        pathParameters(parameterWithName("threadId").description("Id of the Query which needs to be closed."))));

        verify(financeCheckQueriesService).close(threadId);
    }

    @Override
    public void setupMockMvc() {
        controller = new ProjectFinanceQueriesController(financeCheckQueriesService);
        super.setupMockMvc();
    }

    @Override
    protected ProjectFinanceQueriesController supplyControllerUnderTest() {
        return null;
    }
}
