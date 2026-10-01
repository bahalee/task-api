package com.baha.taskapi.controller;

import com.baha.taskapi.entity.Task;
import com.baha.taskapi.enums.TaskStatus;
import com.baha.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Learn Spring Boot",
                                    "description": "Prepare the technical test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void shouldRejectInvalidTitle() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectForbiddenTransition() throws Exception {
        Task task = new Task("Test task", "Test description");
        task = taskRepository.save(task);

        mockMvc.perform(patch("/tasks/" + task.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "DONE"
                                }
                                """))
                .andExpect(status().isConflict());
    }
    @Test
    void shouldAllowTodoToInProgress() throws Exception {
        Task task = new Task("Test task", "Test description");
        task = taskRepository.save(task);

        mockMvc.perform(patch("/tasks/" + task.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "status": "IN_PROGRESS"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldAllowInProgressToDone() throws Exception {
        Task task = new Task("Test task", "Test description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task = taskRepository.save(task);

        mockMvc.perform(patch("/tasks/" + task.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "status": "DONE"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }
    @Test
    void shouldReturnNotFoundForUnknownTask() throws Exception {
        mockMvc.perform(patch("/tasks/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "status": "IN_PROGRESS"
                            }
                            """))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldGetAllTasks() throws Exception {
        taskRepository.save(new Task("Task 1", "Description 1"));
        taskRepository.save(new Task("Task 2", "Description 2"));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFilterTasksByStatus() throws Exception {
        taskRepository.save(new Task("Todo task", "Description"));

        Task doneTask = new Task("Done task", "Description");
        doneTask.setStatus(TaskStatus.DONE);
        taskRepository.save(doneTask);

        mockMvc.perform(get("/tasks")
                        .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }
    @Test
    void shouldRejectUnknownStatusInGet() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnknownStatusInPatch() throws Exception {
        Task task = new Task("Test task", "Description");
        task = taskRepository.save(task);

        mockMvc.perform(patch("/tasks/" + task.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "status": "INVALID"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
}