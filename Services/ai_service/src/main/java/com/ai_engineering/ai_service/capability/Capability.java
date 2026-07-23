package com.ai_engineering.ai_service.capability;

import java.io.IOException;

/**
 * A self-contained AI capability. Each implementation owns everything specific
 * to one capability: which prompt it uses, which tools it exposes, how request
 * fields map to prompt variables, and what output type it returns.
 *
 * <p>Adding a new capability means adding one bean — no controller or engine
 * changes required (the Goal's "Capability Driven" + "Prompt First" principles).
 *
 * @param <REQ> the request DTO type
 * @param <RES> the response DTO type
 */
public interface Capability<REQ extends CapabilityRequest, RES> {

    /** The capability this bean handles; used by the registry for lookup. */
    CapabilityType type();

    /** Execute the capability against the given request. */
    RES execute(REQ request) throws IOException;
}
