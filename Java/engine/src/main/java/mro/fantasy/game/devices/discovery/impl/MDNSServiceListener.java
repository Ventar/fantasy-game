package mro.fantasy.game.devices.discovery.impl;

import mro.fantasy.game.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Abstract class to make the code on the {@link DeviceDiscoveryServiceImpl} more readable since the {@link #serviceAdded(ServiceEvent)} and {@link #serviceRemoved(ServiceEvent)}
 * methods are not needed.
 *
 * @author Michael Rodenbuecher
 * @since 2022-08-19
 */
public class MDNSServiceListener implements ServiceListener {


    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MDNSServiceListener.class);

    /**
     * The implementation utility class for the mDNS service.
     */
    private JmDNS jmdns;

    /**
     * Function called when a service is resolved
     */
    private Consumer<ServiceEvent> resolveFunction;

    /**
     * The service type to resolve.
     */
    private String serviceType;

    /**
     * Creates a new listener.
     *
     * @param jmDNS           The implementation utility class for the mDNS service.
     * @param serviceType     the service type to resolve.
     * @param resolveFunction the function that is executed when a service of the passed type is resolved.
     */
    public MDNSServiceListener(JmDNS jmDNS, String serviceType, Consumer<ServiceEvent> resolveFunction) {

        ValidationUtils.requireNonNull(jmDNS, "The jmDNS cannot be null");
        ValidationUtils.requireNonNull(serviceType, "The service type cannot be null");
        ValidationUtils.requireNonNull(resolveFunction, "The resolve function cannot be null");

        try {
            this.jmdns = jmDNS;
            this.serviceType = serviceType;
            this.resolveFunction = resolveFunction;
            jmdns.addServiceListener(serviceType, this);
        } catch (Exception e) {
            throw new RuntimeException("Cannot register service listener ::=[ " + serviceType + "]: ", e);
        }
    }

    @Override
    public void serviceAdded(ServiceEvent serviceEvent) {

    }

    @Override
    public void serviceRemoved(ServiceEvent serviceEvent) {

    }

    @Override
    public void serviceResolved(ServiceEvent serviceEvent) {
        LOG.debug("[{}] service resolved event received", serviceType.substring(1, serviceType.indexOf(".")).toUpperCase(Locale.ROOT));
        LOG.debug("[{}] - found via MDNS, name ::= [{}], type ::= [{}], addresses ::= [{}], port ::= [{}]",
                serviceEvent.getInfo().getName(),
                serviceEvent.getInfo().getName(),
                serviceEvent.getInfo().getType(),
                serviceEvent.getInfo().getInetAddresses(),
                serviceEvent.getInfo().getPort());
        try {
            resolveFunction.accept(serviceEvent);
        } catch (Exception e) {
            LOG.debug("Exception during service resolution: ", e);
        }

    }

    /**
     * Creates a new listener.
     *
     * @param jmDNS           The implementation utility class for the mDNS service.
     * @param serviceType     the service type to resolve.
     * @param resolveFunction the function that is executed when a service of the passed type is resolved.
     */
    public static MDNSServiceListener registerMDNSListener(JmDNS jmDNS, String serviceType, Consumer<ServiceEvent> resolveFunction) {
        return new MDNSServiceListener(jmDNS, serviceType, resolveFunction);
    }

}

