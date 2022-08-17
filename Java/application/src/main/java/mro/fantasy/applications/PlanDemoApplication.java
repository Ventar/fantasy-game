package mro.fantasy.applications;

import mro.fantasy.game.Position;
import mro.fantasy.game.devices.board.GameBoard;
import mro.fantasy.game.engine.GameLibrary;
import mro.fantasy.game.plan.Plan;
import mro.fantasy.game.plan.PlanDeltaService;
import mro.fantasy.game.plan.TileRotation;
import mro.fantasy.game.plan.impl.ASCIIPlanRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan("mro.fantasy.game")
public class PlanDemoApplication implements CommandLineRunner {

    @Autowired
    private GameLibrary library;

    @Autowired
    private PlanDeltaService deltaService;

    @Autowired
    private GameBoard gameBoard;

    public static void main(String[] args) {
        SpringApplication.run(PlanDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


        ASCIIPlanRenderer renderer = new ASCIIPlanRenderer();

        Plan origPlan = library.getPlanLibrary().getById("BG001");
        Plan changedPlan = origPlan.copy();

        changedPlan.remove(origPlan.getTiles(new Position(4, 1)).get(0));
        changedPlan.assign(library.getById("BGT006"), new Position(3, 5), TileRotation.DEGREE_180);

        System.out.println(deltaService.calculateChange(origPlan, changedPlan));

        renderer.render(origPlan);
        renderer.render(changedPlan);

        System.out.println("Wait for callback....");

        var event = gameBoard.waitForEvent();

        System.out.println("Received event: " + event.get());

        // PlanImpl plan = (PlanImpl) library.getById("BG001");
        // var planMap = plan.toYAMLMap();
        // System.out.println(planMap);
        //
        // ASCIIPlanRenderer renderer = new ASCIIPlanRenderer();
        // //renderer.render(PlanImpl.fromYAMLMap(library, planMap), ASCIIPlanRenderer.RenderOptions.ANCHOR, ASCIIPlanRenderer.RenderOptions.TEMPLATE_NAME);
        // renderer.render(PlanImpl.fromYAMLMap(library, planMap));


    }
}
