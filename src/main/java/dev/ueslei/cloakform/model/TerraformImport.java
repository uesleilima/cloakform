package dev.ueslei.cloakform.model;

public class TerraformImport extends TerraformObject {

    public TerraformImport(String id, String resource, String name) {
        addAttribute("id", id);
        addAttribute("to", String.format("%s.%s", resource, name), AttributeType.REFERENCE);
    }

    @Override
    public String toString() {
        return getAttribute("to").get();
    }
}
