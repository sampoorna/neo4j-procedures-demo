package stats;

public class GetDegree {

    @Context private GraphDatabaseService db;

    // Result class
    public static class Degree {
        public String label;
        // note, that "int" values are not supported
        public long count, max, min = Long.MAX_VALUE;
        
        // method to consume a degree and compute min, max, count
        private void add(long degree) {
            if (degree < min) min = degree;
            if (degree > max) max = degree;
            count ++;
        }
    }

    @Procedure

    public Stream<Degree> degree(String label) {
        // create holder class for results
        Degree degree = new Degree(label);
        // iterate over all nodes with label
        try (ResourceIterator it = db.findNodes(Label.label(label))) {
            while (it.hasNext()) {
                // submit degree to holder for consumption (i.e. max, min, count)
                d.add(it.next().getDegree());
            }
        }
        // we only return a "Stream" of a single element in this case.
        return Stream.of(degree);
    }
}