package org.metadatacenter.model.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.junit.Test;

public class JSONTreeTrimmerTest {

  private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

  private ObjectNode root = nodeFactory.objectNode();
  private ObjectNode o1 = nodeFactory.objectNode();
  private ObjectNode o2 = nodeFactory.objectNode();
  private ObjectNode o3 = nodeFactory.objectNode();
  private ObjectNode o4 = nodeFactory.objectNode();

  private ArrayNode a1 = nodeFactory.arrayNode();
  private ArrayNode a2 = nodeFactory.arrayNode();

  private ValueNode v1 = nodeFactory.textNode("v1");
  private ValueNode v2 = nodeFactory.textNode("v2");
  private ValueNode v3 = nodeFactory.textNode("v3");
  private ValueNode v4 = nodeFactory.textNode("v4");
  private ValueNode v5 = nodeFactory.textNode("v5");
  private ValueNode v6 = nodeFactory.textNode("v6");
  private ValueNode v7 = nodeFactory.textNode("v7");
  private ValueNode v8 = nodeFactory.textNode("v8");

  @Test
  public void shouldPruneNothing() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", v2);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .prune("p3")
        .trim();
    // Assert
    assertThat(output, is(equalTo(root)));
    assertThat(size(output), is(2));
  }

  @Test
  public void shouldPruneValueNode() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", v2); // prune here
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    ObjectNode output = trimmer
        .prune("p2")
        .trim();
    // Assert
    assertThat(output.get("p2"), is(nullValue()));
    assertThat(size(output), is(1));
  }

  @Test
  public void shouldPruneArrayNode() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", a1); // prune here
    a1.add(v2).add(v3);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    ObjectNode output = trimmer
        .prune("p2")
        .trim();
    // Assert
    assertThat(output.get("p2"), is(nullValue()));
    assertThat(size(output), is(1));
  }

  @Test
  public void shouldPruneObjectNode() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", o1); // prune here
    o1.set("p3", v3);
    o1.set("p4", v4);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    ObjectNode output = trimmer
        .prune("p2")
        .trim();
    // Assert
    assertThat(output.get("p2"), is(nullValue()));
    assertThat(size(output), is(1));
  }

  @Test
  public void shouldPruneArrayElement() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", a1);
    a1.add(o1);
    o1.set("p3", v3); // prune here
    o1.set("p4", v4);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    ObjectNode output = trimmer
        .prune("p3")
        .trim();
    // Assert
    assertThat(output.get("p2").get(0).get("p3"), is(nullValue()));
    assertThat(size(output), is(3));
  }

  @Test
  public void shouldPruneAllOccurences() {
    // Arrange
    root.set("p1", v1); // prune here
    root.set("p2", o1);
    root.set("p3", a1);
    o1.set("p1", v1); // prune here
    o1.set("p4", v4);
    a1.add(o2);
    o2.set("p1", v1); // prune here
    o2.set("p5", v5);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    ObjectNode output = trimmer
        .prune("p1")
        .trim();
    // Assert
    assertThat(output.get("p1"), is(nullValue()));
    assertThat(output.get("p2").get("p1"), is(nullValue()));
    assertThat(output.get("p3").get(0).get("p1"), is(nullValue()));
    assertThat(size(output), is(4));
  }

  @Test
  public void shouldPruneMultipleFields() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", o1);
    root.set("p3", o2);
    o1.set("p4", v2); // prune here
    o1.set("p5", v3); // prune here
    o2.set("p4", v4); // prune here
    o2.set("p5", v5); // prune here
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    ObjectNode output = trimmer
        .prune("p4", "p5")
        .trim();
    // Assert
    assertThat(output.get("p2").get("p4"), is(nullValue()));
    assertThat(output.get("p2").get("p5"), is(nullValue()));
    assertThat(output.get("p3").get("p4"), is(nullValue()));
    assertThat(output.get("p3").get("p5"), is(nullValue()));
    assertThat(size(output), is(3));
  }

  @Test
  public void shouldCollapseNothing() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", v2);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p3")
        .trim();
    // Assert
    assertThat(output, is(equalTo(root)));
    assertThat(size(output), is(2));
  }

  @Test
  public void shouldNotCollapseRootField() {
    // Arrange
    root.set("p1", v1);
    root.set("p2", v2);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p2")
        .trim();
    // Assert
    assertThat(output, is(equalTo(root)));
    assertThat(size(output), is(2));
  }

  @Test
  public void shouldCollapseObjectNode_UseCase1() {
    // Arrange
    // {"p1":"v1","p2":{"p3*":{"p5":"v5","p6":"v6"},"p4":"v4"}}
    root.set("p1", v1);
    root.set("p2", o1);
    o1.set("p3", o2); // collapse here
    o1.set("p4", v4);
    o2.set("p5", v5);
    o2.set("p6", v6);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p3")
        .trim();
    // Assert
    // {"p1":"v1","p2":{"p5":"v5","p6":"v6"}}
    assertThat(output.get("p2").isObject(), is(true));
    assertThat(output.get("p2").get("p5").isValueNode(), is(true));
    assertThat(output.get("p2").get("p6").isValueNode(), is(true));
    assertThat(output.get("p2").get("p5"), is(equalTo(v5)));
    assertThat(output.get("p2").get("p6"), is(equalTo(v6)));
    assertThat(size(output), is(4));
  }

  @Test
  public void shouldCollapseObjectNode_UseCase2() {
    // Arrange
    // {"p1":"v1","p2":{"p3":{"p5":"v5","p6":"v6"},"p4#":"v4"}}
    root.set("p1", v1);
    root.set("p2", o1);
    o1.set("p3", o2);
    o1.set("p4", v4); // collapse here
    o2.set("p5", v5);
    o2.set("p6", v6);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p4")
        .trim();
    // Assert
    // {"p1":"v1","p2":"v4"}
    assertThat(output.get("p2").isValueNode(), is(true));
    assertThat(output.get("p2"), is(equalTo(v4)));
    assertThat(size(output), is(2));
  }

  @Test
  public void shouldCollapseObjectNode_UseCase3() {
    // Arrange
    // {"p1":"v1","p2":{"p3":{"p5#":"v5","p6":"v6"},"p4":"v4"}}
    root.set("p1", v1);
    root.set("p2", o1);
    o1.set("p3", o2);
    o1.set("p4", v4);
    o2.set("p5", v5); // collapse here
    o2.set("p6", v6);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p5")
        .trim();
    // Assert
    // {"p1":"v1","p2":"v5"}
    assertThat(output.get("p2").isObject(), is(true));
    assertThat(output.get("p2").get("p3").isValueNode(), is(true));
    assertThat(output.get("p2").get("p4").isValueNode(), is(true));
    assertThat(output.get("p2").get("p3"), is(equalTo(v5)));
    assertThat(output.get("p2").get("p4"), is(equalTo(v4)));
    assertThat(size(output), is(4));
  }

  @Test
  public void shouldCollapseObjectNode_UseCase4() {
    // Arrange
    // {"p1":"v1","p2":{"p3":{"p5":"v5","p6#":"v6"},"p4":"v4"}}
    root.set("p1", v1);
    root.set("p2", o1);
    o1.set("p3", o2);
    o1.set("p4", v4);
    o2.set("p5", v5);
    o2.set("p6", v6); // collapse here
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p6")
        .trim();
    // Assert
    // {"p1":"v1","p2":{"p3":"v6","p4":"v4"}}
    assertThat(output.get("p2").isObject(), is(true));
    assertThat(output.get("p2").get("p3").isValueNode(), is(true));
    assertThat(output.get("p2").get("p4").isValueNode(), is(true));
    assertThat(output.get("p2").get("p3"), is(equalTo(v6)));
    assertThat(output.get("p2").get("p4"), is(equalTo(v4)));
    assertThat(size(output), is(4));
  }

  @Test
  public void shouldCollapseObjectNode_UseCase5() {
    // Arrange
    // {"p1":"v1","p2#":{"p3#":{"p5":"v5","p6":"v6"},"p4":"v4"}}
    root.set("p1", v1);
    root.set("p2", o1);
    o1.set("p3", o2); // collapse here
    o1.set("p4", v4);
    o2.set("p5", v5); // collapse here
    o2.set("p6", v6);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p3", "p5")
        .trim();
    // Assert
    // {"p1":"v1","p2":"v5"}
    assertThat(output.get("p2").isValueNode(), is(true));
    assertThat(output.get("p2"), is(equalTo(v5)));
    assertThat(size(output), is(2));
  }

  @Test
  public void shouldCollapseObjectNode_UseCase6() {
    // Arrange
    // {"p1":"v1","p2":{"p3":{"p5#":"v5","p6":"v6"},"p4#":"v4"}}
    root.set("p1", v1);
    root.set("p2", o1);
    o1.set("p3", o2);
    o1.set("p4", v4); // collapse here
    o2.set("p5", v5); // collapse here
    o2.set("p6", v6);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p4", "p5")
        .trim();
    // Assert
    // {"p1":"v1","p2":"v4"}
    assertThat(output.get("p2").isValueNode(), is(true));
    assertThat(output.get("p2"), is(equalTo(v4)));
    assertThat(size(output), is(2));
  }

  @Test
  public void shouldCollapseObjectElementInArrayNode_UseCase1() {
    // Arrange
    // {"p1":"v1","p2":[{"p3#":"v3"},{"p4":["v4","v5"]},{"p5":{"p6":"v6","p7":"v7"}}]}
    root.set("p1", v1);
    root.set("p2", a1);
    a1.add(o1).add(o2).add(o3);
    o1.set("p3", v3); // collapse here
    o2.set("p4", a2);
    a2.add(v4).add(v5);
    o3.set("p5", o4);
    o4.set("p6", v6);
    o4.set("p7", v7);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p3")
        .trim();
    // Assert
    // {"p1":"v1","p2":["v3",{"p4":["v4","v5"]},{"p5":{"p6":"v6","p7":"v7"}}]}
    assertThat(output.get("p2").get(0), is(equalTo(v3)));
    assertThat(size(output), is(6));
  }

  @Test
  public void shouldCollapseObjectElementInArrayNode_UseCase2() {
    // Arrange
    // {"p1":"v1","p2":[{"p3":"v3"},{"p4#":["v4","v5"]},{"p5":{"p6":"v6","p7":"v7"}}]}
    root.set("p1", v1);
    root.set("p2", a1);
    a1.add(o1).add(o2).add(o3);
    o1.set("p3", v3);
    o2.set("p4", a2); // collapse here
    a2.add(v4).add(v5);
    o3.set("p5", o4);
    o4.set("p6", v6);
    o4.set("p7", v7);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p4")
        .trim();
    // Assert
    // {"p1":"v1","p2":["v3",["v4","v5"],{"p5":{"p6":"v6","p7":"v7"}}]}
    assertThat(output.get("p2").get(1).isArray(), is(true));
    assertThat(output.get("p2").get(1).get(0), is(equalTo(v4)));
    assertThat(output.get("p2").get(1).get(1), is(equalTo(v5)));
    assertThat(size(output), is(6));
  }

  @Test
  public void shouldCollapseObjectElementInArrayNode_UseCase3() {
    // Arrange
    // {"p1":"v1","p2":[{"p3":"v3"},{"p4":["v4","v5"]},{"p5#":{"p6":"v6","p7":"v7"}}]}
    root.set("p1", v1);
    root.set("p2", a1);
    a1.add(o1).add(o2).add(o3);
    o1.set("p3", v3);
    o2.set("p4", a2);
    a2.add(v4).add(v5);
    o3.set("p5", o4); // collapse here
    o4.set("p6", v6);
    o4.set("p7", v7);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p5")
        .trim();
    // Assert
    // {"p1":"v1","p2":["v3",["v4","v5"],{"p6":"v6","p7":"v7"}]}
    assertThat(output.get("p2").get(2).isObject(), is(true));
    assertThat(output.get("p2").get(2).get("p6"), is(equalTo(v6)));
    assertThat(output.get("p2").get(2).get("p7"), is(equalTo(v7)));
    assertThat(size(output), is(6));
  }

  @Test
  public void shouldCollapseObjectElementInArrayNode_UseCase4() {
    // Arrange
    // {"p1":"v1","p2":[{"p3":"v3"},{"p4":["v4","v5"]},{"p5":{"p6#":"v6","p7":"v7"}}]}
    root.set("p1", v1);
    root.set("p2", a1);
    a1.add(o1).add(o2).add(o3);
    o1.set("p3", v3);
    o2.set("p4", a2);
    a2.add(v4).add(v5);
    o3.set("p5", o4);
    o4.set("p6", v6); // collapse here
    o4.set("p7", v7);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p6")
        .trim();
    // Assert
    // {"p1":"v1","p2":[{"p3":"v3"},{"p4":["v4","v5"]},{"p5":"v6"}]}
    assertThat(output.get("p2").get(2).isObject(), is(true));
    assertThat(output.get("p2").get(2).get("p5"), is(equalTo(v6)));
    assertThat(size(output), is(5));
  }

  @Test
  public void shouldCollapseObjectElementInArrayNode_UseCase5() {
    // Arrange
    // {"p1":"v1","p2":[{"p3":"v3"},{"p4":["v4","v5"]},{"p5":{"p6":"v6","p7#":"v7"}}]}
    root.set("p1", v1);
    root.set("p2", a1);
    a1.add(o1).add(o2).add(o3);
    o1.set("p3", v3);
    o2.set("p4", a2);
    a2.add(v4).add(v5);
    o3.set("p5", o4);
    o4.set("p6", v6);
    o4.set("p7", v7); // collapse here
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .collapse("p7")
        .trim();
    // Assert
    // {"p1":"v1","p2":[{"p3":"v3"},{"p4":["v4","v5"]},{"p5":"v7"}]}
    assertThat(output.get("p2").get(2).isObject(), is(true));
    assertThat(output.get("p2").get(2).get("p5"), is(equalTo(v7)));
    assertThat(size(output), is(5));
  }

  @Test
  public void shouldPruneAndCollapse_UseCase1() {
    // Arrange
    // {"p1*":"v1","p2":{"p5#":"v5","p6":"v6"},"p3":["v3","v4"],"p4":[{"p7":"v7"},{"p8":"v8"}]}
    root.set("p1", v1); // prune here
    root.set("p2", o1);
    root.set("p3", a1);
    a1.add(v3).add(v4);
    root.set("p4", a2);
    a2.add(o2).add(o3);
    o1.set("p5", v5); // collapse here
    o1.set("p6", v6);
    o2.set("p7", v7);
    o3.set("p8", v8);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .prune("p1")
        .collapse("p5")
        .trim();
    // Assert
    // {"p2":"v5","p3":["v3","v4"],"p4":[{"p7":"v7"},{"p8":"v8"}]}
    assertThat(output.get("p1"), is(nullValue()));
    assertThat(output.get("p2"), is(equalTo(v5)));
    assertThat(size(output), is(5));
  }

  @Test
  public void shouldPruneAndCollapse_UseCase2() {
    // Arrange
    // {"p1":"v1","p2*":{"p5":"v5","p6":"v6"},"p3":["v3","v4"],"p4":[{"p7#":"v7"},{"p8":"v8"}]}
    root.set("p1", v1);
    root.set("p2", o1); // prune here
    root.set("p3", a1);
    a1.add(v3).add(v4);
    root.set("p4", a2);
    a2.add(o2).add(o3);
    o1.set("p5", v5);
    o1.set("p6", v6);
    o2.set("p7", v7); // collapse here
    o3.set("p8", v8);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .prune("p2")
        .collapse("p7")
        .trim();
    // Assert
    // {"p1":"v1","p3":["v3","v4"],"p4":["v7",{"p8":"v8"}]}
    System.out.println(output);
    assertThat(output.get("p2"), is(nullValue()));
    assertThat(output.get("p4").get(0), is(equalTo(v7)));
    assertThat(size(output), is(4));
  }

  @Test
  public void shouldPruneAndCollapse_UseCase3() {
    // Arrange
    // {"p1":"v1","p2":{"p5#":"v5","p6":"v6"},"p3":["v3","v4"],"p4*":[{"p7":"v7"},{"p8":"v8"}]}
    root.set("p1", v1);
    root.set("p2", o1);
    root.set("p3", a1);
    a1.add(v3).add(v4);
    root.set("p4", a2); // prune here
    a2.add(o2).add(o3);
    o1.set("p5", v5); // collapse here
    o1.set("p6", v6);
    o2.set("p7", v7);
    o3.set("p8", v8);
    // Act
    JSONTreeTrimmer trimmer = new JSONTreeTrimmer(root);
    JsonNode output = trimmer
        .prune("p4")
        .collapse("p5")
        .trim();
    // Assert
    // {"p1":"v1","p2":"v5","p3":["v3","v4"]}
    System.out.println(output);
    assertThat(output.get("p4"), is(nullValue()));
    assertThat(output.get("p2"), is(equalTo(v5)));
    assertThat(size(output), is(3));
  }

  // Private utility methods

  private static int size(JsonNode node) {
    if (node.isObject()) {
      return objectSize((ObjectNode) node);
    } else if (node.isArray()) {
      return arraySize((ArrayNode) node);
    }
    return 0;
  }

  private static int objectSize(ObjectNode node) {
    int count = node.size();
    for (JsonNode child : node) {
      count += size(child);
    }
    return count;
  }

  private static int arraySize(ArrayNode node) {
    int count = 0;
    for (JsonNode element : node) {
      count += size(element);
    }
    return count;
  }
}