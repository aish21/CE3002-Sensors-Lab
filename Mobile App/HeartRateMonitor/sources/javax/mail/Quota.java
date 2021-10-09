package javax.mail;

public class Quota {
    public String quotaRoot;
    public Resource[] resources;

    public static class Resource {
        public long limit;
        public String name;
        public long usage;

        public Resource(String name2, long usage2, long limit2) {
            this.name = name2;
            this.usage = usage2;
            this.limit = limit2;
        }
    }

    public Quota(String quotaRoot2) {
        this.quotaRoot = quotaRoot2;
    }

    public void setResourceLimit(String name, long limit) {
        if (this.resources == null) {
            this.resources = new Resource[1];
            this.resources[0] = new Resource(name, 0, limit);
            return;
        }
        for (int i = 0; i < this.resources.length; i++) {
            if (this.resources[i].name.equalsIgnoreCase(name)) {
                this.resources[i].limit = limit;
                return;
            }
        }
        Resource[] ra = new Resource[(this.resources.length + 1)];
        System.arraycopy(this.resources, 0, ra, 0, this.resources.length);
        ra[ra.length - 1] = new Resource(name, 0, limit);
        this.resources = ra;
    }
}
