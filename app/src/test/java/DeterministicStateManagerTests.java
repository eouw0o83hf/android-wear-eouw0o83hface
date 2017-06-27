import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.eouw0o83hf.eouw0o83hface.BackgroundShape;
import com.eouw0o83hf.eouw0o83hface.BackgroundShapeManager;
import com.eouw0o83hf.eouw0o83hface.DeterministicStateManager;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.eouw0o83hf.eouw0o83hface.DeterministicStateManager.VisualState.*;

public class DeterministicStateManagerTests extends TestCase {

    private class Context {
        public List<TestBackgroundShape> BackgroundShapes = Arrays.asList(
                new TestBackgroundShape(),
                new TestBackgroundShape()
        );

        public CheaterDeterministicStateManager Sut = new CheaterDeterministicStateManager(BackgroundShapes);
    }

    private class CheaterDeterministicStateManager extends DeterministicStateManager {
        public CheaterDeterministicStateManager(Collection<TestBackgroundShape> background) {
            super(new TestBackgroundManager(background));
        }

        public void ChangeState(VisualState state) {
            super.ChangeModeTo(state);
        }
    }

    private class TestBackgroundShape implements BackgroundShape {

        @Override
        public void Render(Canvas canvas, Paint sharedPaint) { }

        public boolean IsActive = true;

        @Override
        public boolean GetActive() {
            return IsActive;
        }
    }

    private class TestBackgroundManager implements BackgroundShapeManager {

        private final Collection<TestBackgroundShape> _background;

        public TestBackgroundManager(Collection<TestBackgroundShape> background) {
            _background = background;
        }

        @Override
        public Collection<? extends BackgroundShape> getBackgroundShapes() {
            return _background;
        }

        @Override
        public void initialize(Rect bounds) { }

        @Override
        public void randomize() { }

        @Override
        public boolean turnOnOrOff() { return false; }

        @Override
        public void incrementState(StateChangeStatus changeStatus) { }
    }

    public void test_state_defaultsToInteractive() {
        Context context = new Context();
        assertEquals(Interactive, context.Sut.GetState());
    }

    public void test_ambientModeChanged_fromAmbient_movesToLeavingInteractive() {
        Context context = new Context();
        context.Sut.AmbientModeChanged(true);
        assertEquals(LeavingInteractive, context.Sut.GetState());
    }

    public void test_ambientModeChanged_fromActive_movesToEnteringInteractive() {
        Context context = new Context();
        context.Sut.AmbientModeChanged(false);
        assertEquals(EnteringInteractive, context.Sut.GetState());
    }

    public void test_refreshStateFromBackground_fromInteractive_doesNotChange() {
        Context context = new Context();
        context.Sut.RefreshStateFromBackground();
        assertEquals(Interactive, context.Sut.GetState());
    }

    public void test_refreshStateFromBackground_fromAmbient_doesNotChange() {
        Context context = new Context();
        context.Sut.ChangeState(Ambient);
        context.Sut.RefreshStateFromBackground();
        assertEquals(Ambient, context.Sut.GetState());
    }

    public void test_refreshStateFromBackground_fromIntermediateState_givenNonMatchingShapes_doesntChange() {
        Context context = new Context();
        context.BackgroundShapes.get(0).IsActive = false;
        context.Sut.ChangeState(EnteringInteractive);
        context.Sut.RefreshStateFromBackground();
        assertEquals(EnteringInteractive, context.Sut.GetState());

        context.Sut.ChangeState(LeavingInteractive);
        context.Sut.RefreshStateFromBackground();
        assertEquals(LeavingInteractive, context.Sut.GetState());
    }

    public void test_refreshStateFromBackground_fromIntermediateState_givenActiveShapes_becomesInteractive() {
        Context context = new Context();
        context.Sut.ChangeState(EnteringInteractive);
        context.Sut.RefreshStateFromBackground();
        assertEquals(Interactive, context.Sut.GetState());

        context.Sut.ChangeState(LeavingInteractive);
        context.Sut.RefreshStateFromBackground();
        assertEquals(Interactive, context.Sut.GetState());
    }

    public void test_refreshStateFromBackground_fromIntermediateState_givenInactiveShapes_becomesAmbient() {
        Context context = new Context();
        for (TestBackgroundShape backgroundShape : context.BackgroundShapes) {
            backgroundShape.IsActive = false;
        }
        context.Sut.ChangeState(LeavingInteractive);
        context.Sut.RefreshStateFromBackground();
        assertEquals(Ambient, context.Sut.GetState());

        context.Sut.ChangeState(EnteringInteractive);
        context.Sut.RefreshStateFromBackground();
        assertEquals(Ambient, context.Sut.GetState());
    }
}
