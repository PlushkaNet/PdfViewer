package app.grapheneos.pdfviewer.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.grapheneos.pdfviewer.currentPage
import app.grapheneos.pdfviewer.testrules.RetryRules
import app.grapheneos.pdfviewer.RetryableComposeRule
import app.grapheneos.pdfviewer.testrules.OrientationRules
import app.grapheneos.pdfviewer.util.PdfViewerLauncher
import app.grapheneos.pdfviewer.util.PdfViewerRobot
import app.grapheneos.pdfviewer.util.PdfViewerTestUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * Recently opened files list on the home screen.
 *
 * Intents are causing scenario.close() to hang for ~45 second.
 * By releasing Intents before the scenario closes, we avoid this.
 */
@RunWith(AndroidJUnit4::class)
class PdfViewerRecentsTest {

    private val composeRule = RetryableComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain
        .outerRule(RetryRules())
        .around(OrientationRules())
        .around(composeRule)

    private val robot = PdfViewerRobot(composeRule)

    @Before
    fun setup() {
        PdfViewerTestUtils.init(composeRule)
    }

    @Test
    fun openedFile_appearsInRecentList_withLastPage() {
        PdfViewerLauncher.launchDefault().use { scenario ->
            robot.assertHomeScreenShown()

            robot.openDocumentFromHome(PdfViewerLauncher.testAssetUri("test-multipage.pdf"))
            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)

            scenario.onActivity {
                it.currentPage = 3
            }
            composeRule.waitForIdle()

            robot.pressBack()

            robot.assertRecentFilePage(3)
        }
    }

    @Test
    fun recentFile_openedFromList_restoresLastPage() {
        PdfViewerLauncher.launchDefault().use { scenario ->
            robot.openDocumentFromHome(PdfViewerLauncher.testAssetUri("test-multipage.pdf"))
            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)

            scenario.onActivity {
                it.currentPage = 3
            }
            composeRule.waitForIdle()

            robot.pressBack()
            robot.clickRecentFile(0)

            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)
            scenario.onActivity {
                assertEquals(3, it.currentPage)
            }
        }
    }

    @Test
    fun recentFile_canBeRemovedFromList() {
        PdfViewerLauncher.launchDefault().use { scenario ->
            robot.openDocumentFromHome(PdfViewerLauncher.testAssetUri("test-multipage.pdf"))
            PdfViewerTestUtils.waitForDocumentFullyLoaded(scenario)

            robot.pressBack()
            robot.assertRecentFilePage(1)

            robot.removeRecentFile(0)

            robot.assertHomeScreenShown()
        }
    }
}
