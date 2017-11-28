package stats;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemRelevance_Calculateitem {

    @Context
    public org.neo4j.graphdb.GraphDatabaseService db;

        @Procedure
        public HashMap<Item, Double> ItemRelevance_CalculateItem(Integer itemID, ArrayList<Integer> relatedItemIDs, Double commonItemTypeWeight, Double commonTagsWeight) {

            HashMap<Item, Double> result = new HashMap<>();

            // Get the target item
            Item targetItem = new Item(itemID);

            for (Integer relatedItemID : relatedItemIDs) {
                Item candidateItem = new Item(relatedItemID);
                result.replace(candidateItem, targetItem.GetSimilarityScore(candidateItem, commonItemTypeWeight, commonTagsWeight));
            }

            return result;
    }

    public class Item
    {
        public int ID;
        public Node Node;
        public int ItemType;
        public Set<String> Tags;

        // Constructors
        public Item(int ID)
        {
            this.ID = ID;
            this.Node = db.findNode(Label.label("Item"), "ID", ID);
        }

        private void SetItemType(){
            Relationship hasAnRelationship = this.Node.getRelationships(RelationshipType.withName("HAS_AN"), Direction.OUTGOING).iterator().next();
            Node targetItemType = hasAnRelationship.getOtherNode(this.Node);

            this.ItemType = Integer.parseInt(targetItemType.getProperty("ID").toString());
        }

        public int GetItemType(){
            if (ItemType == 0)
                SetItemType();

            return ItemType;
        }

        private void SetTags(){
            Iterable<Relationship> taggedRelationships = this.Node.getRelationships(RelationshipType.withName("TAGGED"), Direction.OUTGOING);

            Stream<Relationship> relationshipStream = StreamSupport.stream(taggedRelationships.spliterator(), false);
            this.Tags = relationshipStream.map(r -> r.getEndNode().getProperty("Label").toString()).collect(Collectors.toSet());
        }

        public Set<String> GetTags() {

            if (Tags == null)
                SetTags();

            return Tags;
        }

        public int GetTagCount(){
            return Tags.size();
        }

        public Double GetSimilarityScore(Item relatedItem, Double commonItemTypeWeight, Double commonTagsWeight){
            double score = 0.0;

            if (this.ItemType == relatedItem.GetItemType())
                score += commonItemTypeWeight;

            Set commonTags = new HashSet<String>(this.GetTags());
            commonTags.retainAll(relatedItem.GetTags());
            score += (commonTags.size() * commonTagsWeight)/(this.GetTagCount() + relatedItem.GetTagCount());

            return score/(commonItemTypeWeight + commonTagsWeight);
        }
    }
}
