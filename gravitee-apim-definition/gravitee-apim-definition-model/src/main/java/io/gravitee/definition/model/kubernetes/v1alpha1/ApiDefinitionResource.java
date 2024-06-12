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
package io.gravitee.definition.model.kubernetes.v1alpha1;

import static io.gravitee.kubernetes.mapper.GroupVersionKind.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.gravitee.kubernetes.mapper.CustomResource;
import io.gravitee.kubernetes.mapper.ObjectMeta;
import java.util.List;

/**
 * @author GraviteeSource Team
 */
public class ApiDefinitionResource extends CustomResource<ObjectNode> {

    private static final List<String> UNSUPPORTED_API_FIELDS = List.of(
        "definition_context",
        "execution_mode",
        "primaryOwner",
        "createdAt",
        "updatedAt",
        "picture",
        "apiMedia"
    );

    private static final List<String> UNSUPPORTED_PLAN_FIELDS = List.of("created_at", "updated_at", "published_at", "definitionVersion");

    private static final List<String> UNSUPPORTED_PAGE_FIELDS = List.of(
        "lastContributor",
        "lastModificationDate",
        "parentPath",
        "excludedAccessControls",
        "accessControls",
        "attached_media",
        "contentType"
    );

    private static final String PLANS_FIELD = "plans";

    private static final String PAGES_FIELD = "pages";
    private static final String MEMBERS_FIELD = "members";

    private static final String NAME_FIELD = "name";

    private static final String METADATA_FIELD = "metadata";

    private static final List<String> UNSUPPORTED_METADATA_FIELDS = List.of("apiId");

    private static final List<String> API_ID_FIELDS = List.of("crossId", "id");

    private static final List<String> PLAN_ID_FIELDS = List.of("crossId", "id", "api");

    private static final String VERSION_FIELD = "version";

    private static final String CONTEXT_REF_FIELD = "contextRef";

    private static final String CONTEXT_NAME_FIELD = "name";

    private static final String CONTEXT_NAMESPACE_FIELD = "namespace";

    private static final String PROXY_FIELD = "proxy";

    private static final String VIRTUAL_HOSTS_FIELD = "virtual_hosts";

    private static final String VIRTUAL_HOST_PATH_FIELD = "path";

    public ApiDefinitionResource(String name, ObjectNode apiDefinition) {
        super(GIO_V1_ALPHA_1_API_DEFINITION, new ObjectMeta(name), apiDefinition);
        removeUnsupportedFields();
    }

    public void setContextPath(String contextPath) {
        ObjectNode proxy = getSpec().get(PROXY_FIELD).deepCopy();
        ArrayNode virtualHosts = (ArrayNode) proxy.get((VIRTUAL_HOSTS_FIELD));
        ObjectNode virtualHost = ((ObjectNode) virtualHosts.iterator().next()).put(VIRTUAL_HOST_PATH_FIELD, contextPath);
        virtualHost.put(VIRTUAL_HOST_PATH_FIELD, contextPath);
        getSpec().set(PROXY_FIELD, proxy);
    }

    public void setVersion(String version) {
        getSpec().put(VERSION_FIELD, version);
    }

    public void setContextRef(String name, String namespace) {
        getSpec().putObject(CONTEXT_REF_FIELD).put(CONTEXT_NAME_FIELD, name).put(CONTEXT_NAMESPACE_FIELD, namespace);
    }

    public boolean hasMembers() {
        return getSpec().hasNonNull(MEMBERS_FIELD);
    }

    // There is a weird behavior with jackson that forces us to pass back the spec instead of using the class getter
    // If we don't do this, the member object gets duplicated at different levels on serialization.
    // To be investigated ...
    public ArrayNode getMembers(ObjectNode spec) {
        return (ArrayNode) spec.get(MEMBERS_FIELD);
    }

    public void replaceMembers(ArrayNode members) {
        getSpec().replace(MEMBERS_FIELD, members);
    }

    public void removeIds() {
        ObjectNode spec = getSpec();

        spec.remove(API_ID_FIELDS);

        if (spec.hasNonNull(PLANS_FIELD)) {
            spec.get(PLANS_FIELD).forEach(plan -> ((ObjectNode) plan).remove(PLAN_ID_FIELDS));
        }
    }

    private void removeUnsupportedFields() {
        ObjectNode spec = getSpec();

        spec.remove(UNSUPPORTED_API_FIELDS);

        if (spec.hasNonNull(PLANS_FIELD)) {
            spec.get(PLANS_FIELD).forEach(plan -> ((ObjectNode) plan).remove(UNSUPPORTED_PLAN_FIELDS));
        }

        if (spec.hasNonNull(PAGES_FIELD)) {
            spec.replace(PAGES_FIELD, mapPages((ArrayNode) spec.get(PAGES_FIELD)));
        }

        if (spec.hasNonNull(METADATA_FIELD)) {
            spec.get(METADATA_FIELD).forEach(meta -> ((ObjectNode) meta).remove(UNSUPPORTED_METADATA_FIELDS));
        }
    }

    private JsonNode mapPages(ArrayNode pages) {
        var pagesMap = JsonNodeFactory.instance.objectNode();
        for (var page : pages) {
            pagesMap.set(page.get(NAME_FIELD).asText(), ((ObjectNode) page).remove(UNSUPPORTED_PAGE_FIELDS));
        }
        return pagesMap;
    }
}
