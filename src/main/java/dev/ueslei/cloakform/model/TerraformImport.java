package dev.ueslei.cloakform.model;

public class TerraformImport extends TerraformObject {

    private final String name;

    public TerraformImport(String id, String resource, String name) {
        this.name = name;
        super.getAttributes().put("id", id);
        super.getAttributes().put("to", String.format("%s.%s", resource, name));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
