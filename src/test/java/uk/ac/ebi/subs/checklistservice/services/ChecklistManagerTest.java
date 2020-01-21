package uk.ac.ebi.subs.checklistservice.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.checklistservice.models.ChecklistSchemaValidationException;
import uk.ac.ebi.subs.checklistservice.models.ChecklistValidationException;
import uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist.ArchiveChecklistService;
import uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist.ena.UsiChecklistGeneratorService;
import uk.ac.ebi.subs.checklistservice.services.internal.notification.NotificationService;
import uk.ac.ebi.subs.checklistservice.services.internal.validation.ChecklistValidationService;
import uk.ac.ebi.subs.checklistservice.test.config.Config;
import uk.ac.ebi.subs.repository.model.ArchivedChecklist;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.repos.ArchivedChecklistRepository;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Config.class})
@TestPropertySource(locations="classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ChecklistManagerTest {

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private ArchivedChecklistRepository archivedChecklistRepository;

    @Autowired
    private ChecklistManager checklistManager;

    @MockBean
    private ArchiveChecklistService archiveChecklistService;

    @MockBean
    private ChecklistValidationService validationService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private UsiChecklistGeneratorService usiChecklistGeneratorService;

    @Before
    public void before() {
        checklistRepository.deleteAll();
        archivedChecklistRepository.deleteAll();
    }

    @Test
    public void testSuccessful() {
        String checklistId = "cl-01";

        // stored and updated must be different objects.
        Checklist stored = new Checklist();
        stored.setId(checklistId);

        Checklist updated = new Checklist();
        updated.setId(checklistId);

        checklistRepository.save(stored);

        Mockito.when(archiveChecklistService.getUpdatedChecklists()).thenReturn(Arrays.asList(updated));

        checklistManager.updateChecklists();

        List<ArchivedChecklist> archivedChecklists = archivedChecklistRepository.findAll();
        Assert.assertTrue(archivedChecklists.size() == 1);

        ArchivedChecklist archivedChecklist = archivedChecklists.get(0);
        Assert.assertNotNull(archivedChecklist.getChecklist());
        Assert.assertEquals(stored.getId(), archivedChecklist.getChecklist().getId());
        Assert.assertEquals(stored.getVersion(), archivedChecklist.getChecklist().getVersion());

        updated = checklistRepository.findById(checklistId).orElse(null);
        Assert.assertNotNull(updated);
        Assert.assertEquals(archivedChecklist.getChecklist().getVersion() + 1, updated.getVersion().longValue());
    }

    @Test
    public void testValidationFailure() {
        String checklistId = "cl-01";

        Checklist stored = new Checklist();
        stored.setId(checklistId);

        checklistRepository.save(stored);

        Mockito.when(archiveChecklistService.getUpdatedChecklists()).thenReturn(Arrays.asList(stored));
        Mockito.doThrow(new ChecklistSchemaValidationException(null, null, null)).when(validationService).validate(Mockito.any());

        checklistManager.updateChecklists();

        stored = checklistRepository.findById(checklistId).orElse(null);
        Assert.assertNotNull(stored);
        Assert.assertEquals(true, stored.getOutdated());
    }
}
