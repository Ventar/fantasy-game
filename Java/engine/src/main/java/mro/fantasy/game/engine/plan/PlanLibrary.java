package mro.fantasy.game.engine.plan;

import mro.fantasy.game.resources.impl.DefaultResourceLibrary;
import org.springframework.stereotype.Component;

/**
 * Marker class to make the handling of the generic resource library easier.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-05
 */
@Component
public class PlanLibrary extends DefaultResourceLibrary<Plan> {

}
