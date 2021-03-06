package de.agilecoders.wicket.logging;

import org.apache.wicket.request.IRequestParameters;

import java.util.HashSet;
import java.util.Set;

/**
 * An {@link IParamValueExtractor} extracts key/values and parses given
 * {@link IRequestParameters}.
 *
 * @author miha
 */
public interface IParamValueExtractor {

    /**
     * parses given parameters
     *
     * @param params the parameters to parse
     * @return result that contains all log messages and the client information
     */
    Result parse(IRequestParameters params);

    /**
     * Default implementation of {@link IParamValueExtractor}
     */
    public static final class DefaultParamValueExtractor implements IParamValueExtractor {

        @Override
        public Result parse(IRequestParameters params) {
            final Set<ClientSideLogObject> logObjects = new HashSet<>();
            final ClientInfos clientInfos = new ClientInfos();

            for (final String paramName : params.getParameterNames()) {
                final String realParamName = extractRealParamName(paramName);

                switch (realParamName) {
                    case ParamNames.TIMESTAMP:
                    case ParamNames.LEVEL:
                    case ParamNames.MESSAGE:
                        final int index = extractIndex(paramName);

                        if (index > -1) {
                            logObjects.add(new ClientSideLogObject(params.getParameterValue(ParamNames.LEVEL + DefaultValues.paramSplitter + index),
                                                                   params.getParameterValue(ParamNames.MESSAGE + DefaultValues.paramSplitter + index),
                                                                   params.getParameterValue(ParamNames.TIMESTAMP + DefaultValues.paramSplitter + index)));
                        }
                        break;

                    case ParamNames.AJAX_BASE_URL:
                    case ParamNames.SCREEN_SIZE:
                    case ParamNames.WINDOW_SIZE:
                    case ParamNames.USER_AGENT:
                        clientInfos.put(paramName, params.getParameterValue(paramName).toString(DefaultValues.defaultClientInfoValue));
                }
            }

            return new Result(logObjects, clientInfos);
        }

        private int extractIndex(String paramName) {
            final int index = paramName.indexOf(DefaultValues.paramSplitterChar);

            if (index > -1) {
                return Integer.parseInt(paramName.substring(index + 1));
            } else {
                return -1;
            }
        }

        private String extractRealParamName(final String paramName) {
            final int index = paramName.indexOf(DefaultValues.paramSplitterChar);
            if (index >= 0) {
                return paramName.substring(0, index);
            }

            return paramName;
        }
    }

    /**
     * result of {@link IParamValueExtractor#parse(org.apache.wicket.request.IRequestParameters)}
     */
    public static final class Result {
        private final Iterable<ClientSideLogObject> logObjects;
        private final ClientInfos clientInfos;

        /**
         * Construct.
         *
         * @param logObjects  all log messages
         * @param clientInfos the client information
         */
        public Result(Iterable<ClientSideLogObject> logObjects, ClientInfos clientInfos) {
            this.logObjects = logObjects;
            this.clientInfos = clientInfos;
        }

        /**
         * @return the client information
         */
        public ClientInfos clientInfos() { return clientInfos; }

        /**
         * @return all log messages
         */
        public Iterable<ClientSideLogObject> logObjects() { return logObjects; }
    }
}
