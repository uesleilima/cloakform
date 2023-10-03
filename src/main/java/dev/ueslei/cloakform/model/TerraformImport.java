package dev.ueslei.cloakform.model;

public class TerraformImport extends TerraformObject {

    private final String name;

    public TerraformImport(String id, String resource, String name) {
        this.name = name;
        addAttribute("id", id);
        addAttribute("to", String.format("%s.%s", resource, name), AttributeType.REFERENCE);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
