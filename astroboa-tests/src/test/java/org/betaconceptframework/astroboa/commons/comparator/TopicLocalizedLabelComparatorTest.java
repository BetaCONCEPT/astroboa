package org.betaconceptframework.astroboa.commons.comparator;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TopicLocalizedLabelComparatorTest {

  @Test
  public void itSortsCorrectlyGreekTopicLabels() throws Exception {
    Collator greekCollator = Collator.getInstance(new Locale("el", "GR"));
    String[] greekLabelsInRightOrder = new String[] {"Ἀγαθόκλεα", "ἀγαθός", "Ἀγάθων", "ἀγών", "Ἀδαῖος", "άδελφή", "Ἀδριανός", "Ἅδυμος", "Ἀδύρα", "Αίλίας", "ἀκατάφθορος", "ἑαυτός", "ἔκγονος", "Ἑρμουγένης", "Εὔξενος", "ἥρως", "ἵππος", "Ἶσις", "Ἰώριος", "ὑγιαίνω", "υἱός", "ὡνή"};
    String[] greekLabelsInWrongOrder = new String[] {"ἑαυτός", "ἀγαθός", "Ἀγάθων", "Ἀδύρα", "ἀγών", "Ἀδαῖος", "άδελφή", "Ἀδριανός", "Ἅδυμος", "Αίλίας", "ἵππος", "ἀκατάφθορος", "ἔκγονος", "Ἑρμουγένης", "Εὔξενος", "ἥρως", "Ἶσις", "Ἰώριος", "ὑγιαίνω", "υἱός", "ὡνή", "Ἀγαθόκλεα"};

    List<String> greekLabelListInWrongOrder = Arrays.asList(greekLabelsInWrongOrder);
    Collections.sort(greekLabelListInWrongOrder, greekCollator);
    Assert.assertEquals(greekLabelListInWrongOrder, Arrays.asList(greekLabelsInRightOrder));
  }

}