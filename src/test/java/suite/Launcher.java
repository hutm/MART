package suite;/*
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import org.mart.crs.utils.helper.HelperFile;
import org.testng.TestNG;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * @version 1.0 3/26/12 3:47 PM
 * @author: Hut
 */
public class Launcher {

    public static void main(String[] args) throws Exception {
        final TestNG testNG = new TestNG(true);
        final Parser parser = new Parser(HelperFile.getResourceFile(MartSuiteTest.TESTNG_XML_PATH).getPath());
        final List<XmlSuite> suites = parser.parseToList();
        testNG.setXmlSuites(suites);
        testNG.run();
    }


}
