package io.github.damson.hardwareinsets.sample.ui

import androidx.compose.ui.unit.dp
import io.github.damson.hardwareinsets.domain.ScreenEdge
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * The label and the corner row are placed off different things, so what the
 * label owes the row is a difference rather than a constant.
 */
class CornerReservationTest {

    private val row = 88.dp

    @Test
    fun `a policy that applies nothing pays the bar inset and the whole row`() {
        assertThat(reservedForTheCornerControls(barInset = 48.dp, everythingInset = 0.dp, rowMoved = 0.dp, applied = 0.dp))
            .isEqualTo(48.dp + row)
    }

    @Test
    fun `an inset the policy already applied is not paid twice`() {
        // The case that shipped wrong: a cutout inset deep enough to clear the
        // status bar had already moved the label, and the reservation added a
        // whole bar inset on top of it.
        assertThat(reservedForTheCornerControls(barInset = 48.dp, everythingInset = 0.dp, rowMoved = 0.dp, applied = 48.dp))
            .isEqualTo(row)
    }

    @Test
    fun `hardware that drives the row inward is room the label leaves it`() {
        assertThat(reservedForTheCornerControls(barInset = 48.dp, everythingInset = 0.dp, rowMoved = 30.dp, applied = 48.dp))
            .isEqualTo(row + 30.dp)
    }

    @Test
    fun `a policy that has already cleared the row reserves nothing`() {
        assertThat(reservedForTheCornerControls(barInset = 48.dp, everythingInset = 0.dp, rowMoved = 0.dp, applied = 400.dp))
            .isEqualTo(0.dp)
    }

    @Test
    fun `the reservation never goes negative and pulls the label back up`() {
        assertThat(reservedForTheCornerControls(barInset = 0.dp, everythingInset = 0.dp, rowMoved = 0.dp, applied = 1000.dp))
            .isGreaterThanOrEqualTo(0.dp)
    }

    @Test
    fun `the settings button standing further in is what the label leaves room for`() {
        // The row clears the bars and the settings button clears everything, so
        // with the bars hidden and a cutout on this edge the button is the one
        // standing further in. Reserving for the row alone put the label over
        // it, which is the regression that giving the button its clearance
        // introduced.
        assertThat(
            reservedForTheCornerControls(
                barInset = 0.dp,
                everythingInset = 53.dp,
                rowMoved = 0.dp,
                applied = 53.dp,
            ),
        ).isEqualTo(row)
    }

    @Test
    fun `whichever corner control stands further in is the one that decides`() {
        val rowIsFurther = reservedForTheCornerControls(
            barInset = 48.dp, everythingInset = 10.dp, rowMoved = 0.dp, applied = 0.dp,
        )
        val buttonIsFurther = reservedForTheCornerControls(
            barInset = 10.dp, everythingInset = 48.dp, rowMoved = 0.dp, applied = 0.dp,
        )
        assertThat(rowIsFurther).isEqualTo(buttonIsFurther).isEqualTo(48.dp + row)
    }

    @Test
    fun `a top anchored label leaves nothing for the settings button`() {
        // The button is on the bottom edge whatever the label does, and LEFT is
        // placed at the top, so neither has the button beside it.
        assertThat(settingsButtonReachOn(ScreenEdge.TOP, atTheBottom = 53.dp)).isEqualTo(0.dp)
        assertThat(settingsButtonReachOn(ScreenEdge.LEFT, atTheBottom = 53.dp)).isEqualTo(0.dp)
    }

    @Test
    fun `a bottom anchored label leaves the button its whole reach`() {
        assertThat(settingsButtonReachOn(ScreenEdge.BOTTOM, atTheBottom = 53.dp)).isEqualTo(53.dp)
        assertThat(settingsButtonReachOn(ScreenEdge.RIGHT, atTheBottom = 53.dp)).isEqualTo(53.dp)
    }
}
