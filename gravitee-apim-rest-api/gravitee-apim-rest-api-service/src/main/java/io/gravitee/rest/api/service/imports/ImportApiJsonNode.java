/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.service.imports;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.gravitee.definition.model.DefinitionContext;
import io.swagger.util.Json;
import java.util.List;

public class ImportApiJsonNode extends ImportJsonNodeWithIds {

    public static final String PLANS = "plans";
    public static final String PAGES = "pages";
    public static final String METADATA = "metadata";
    public static final String VIEWS = "views";
    public static final String API_MEDIA = "apiMedia";
    public static final String MEMBERS = "members";
    public static final String DEFINITION_CONTEXT = "definition_context";

    public static final String DEFINITION_CONTEXT_ORIGIN = "origin";

    public ImportApiJsonNode(JsonNode jsonNode) {
        super(jsonNode);
    }

    public boolean hasPlans() {
        return hasArray(PLANS);
    }

    public List<ImportPlanJsonNode> getPlans() {
        return getChildNodesByName(PLANS).stream().map(ImportPlanJsonNode::new).collect(toList());
    }

    public boolean hasPages() {
        return hasArray(PAGES);
    }

    public boolean hasDefinitionContext() {
        return getJsonNode().hasNonNull(DEFINITION_CONTEXT);
    }

    public JsonNode getDefinitionContext() {
        return getJsonNode().get(DEFINITION_CONTEXT);
    }

    public String getDefinitionContextOrigin() {
        return getJsonNode().findPath(DEFINITION_CONTEXT).findPath(DEFINITION_CONTEXT_ORIGIN).asText();
    }

    public boolean isKubernetesOrigin() {
        return DefinitionContext.ORIGIN_KUBERNETES.equalsIgnoreCase(getDefinitionContextOrigin());
    }

    public List<ImportJsonNodeWithIds> getPages() {
        return getChildNodesWithIdByName(PAGES);
    }

    public ArrayNode getPagesArray() {
        return getArray(PAGES);
    }

    public List<ImportJsonNode> getMetadata() {
        return getChildExtendedNodesByName(METADATA);
    }

    public List<ImportJsonNode> getViews() {
        return getChildExtendedNodesByName(VIEWS);
    }

    public List<ImportJsonNode> getMedia() {
        return getChildExtendedNodesByName(API_MEDIA);
    }

    public List<ImportJsonNode> getMembers() {
        return getChildExtendedNodesByName(MEMBERS);
    }

    public boolean hasMembers() {
        return hasArray(MEMBERS);
    }

    private List<ImportJsonNodeWithIds> getChildNodesWithIdByName(String fieldName) {
        return getChildNodesByName(fieldName).stream().map(ImportJsonNodeWithIds::new).collect(toList());
    }

    private List<ImportJsonNode> getChildExtendedNodesByName(String fieldName) {
        return getChildNodesByName(fieldName).stream().map(ImportJsonNode::new).collect(toList());
    }
}
