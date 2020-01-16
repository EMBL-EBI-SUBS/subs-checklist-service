package uk.ac.ebi.subs.checklistservice.services.internal.validation.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist.ena.UsiChecklistGeneratorService;
import uk.ac.ebi.subs.checklistservice.test.config.Config;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Config.class})
@TestPropertySource(locations="classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ChecklistSchemaValidationDataProviderTest {

    @Autowired
    private ChecklistSchemaValidationDataProvider dataProvider;

    @Autowired
    private ResourceLoader resourceLoader;

    @MockBean
    private ChecklistRepository checklistRepository;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private UsiChecklistGeneratorService usiChecklistGeneratorService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    public void test() throws IOException {
        String checklistId = "ERC000012";

        String resourcesDataPath = "classpath:validation/samples/" + checklistId + "/*.json";

        Resource[] dirDataResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources(resourcesDataPath);

        Checklist checklist = new Checklist();
        checklist.setId(checklistId);

        List<JsonNode> data = dataProvider.getData(checklist);

        Assert.assertFalse(data.isEmpty());
        Assert.assertEquals(dirDataResources.length, data.size());
    }
}
