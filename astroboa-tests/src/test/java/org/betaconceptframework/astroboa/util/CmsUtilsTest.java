package org.betaconceptframework.astroboa.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.Collator;
import java.util.Locale;

/**
 * Created by gchomatas on 25/01/15.
 */
public class CmsUtilsTest {

  @Test
  public void testToLowerCase() throws Exception {
    Assert.assertEquals(CmsUtils.toLowerCase("ἀ".charAt(0)), "α".charAt(0));
    Assert.assertEquals(CmsUtils.toLowerCase("Ἀ".charAt(0)), "α".charAt(0));
    Assert.assertEquals(CmsUtils.toLowerCase("Ά".charAt(0)), "α".charAt(0));
    Assert.assertEquals(CmsUtils.toLowerCase("ῖ".charAt(0)), "ι".charAt(0));
    Assert.assertEquals(CmsUtils.toLowerCase("ὕ".charAt(0)), "υ".charAt(0));
    Assert.assertEquals(CmsUtils.toLowerCase("ὁ".charAt(0)), "ο".charAt(0));
    Assert.assertEquals(CmsUtils.toLowerCase("ἡ".charAt(0)), "η".charAt(0));

  }

  @Test
  public void testCollator() throws Exception {
    Collator testCollator = Collator.getInstance(new Locale("el", "GR"));
    Assert.assertEquals(testCollator.compare("Ἀ", "α"), 0);
  }


}
