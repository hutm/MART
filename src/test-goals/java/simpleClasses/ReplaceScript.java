/*
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

package simpleClasses;

import org.mart.crs.utils.helper.HelperFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0 10/2/11 8:33 PM
 * @author: Hut
 */
public class ReplaceScript {

    public static void writeScript(String fileFrom, String fileto, String fileOut){
        List<String> fromValues = HelperFile.readLinesFromTextFile(fileFrom);
        List<String> toValues = HelperFile.readLinesFromTextFile(fileto);

        List<String> outList = new ArrayList<String>();
//        outList.add("for fl in `find . -name *.tex`");

        for(int i = 0; i < fromValues.size(); i++){
//            outList.add("sed 's/" + fromValues.get(i) + "/" + toValues.get(i) + "/g' $fl");
            outList.add("sed -i 's/cite{" + fromValues.get(i) + "}/cite{" + toValues.get(i) + "}/g'  `find . -name *.tex` ");

        }

//        outList.add("done");
        HelperFile.saveCollectionInFile(outList, fileOut, false);
    }


    public static void main(String[] args) {
        writeScript("/home/hut/temp1.txt", "/home/hut/temp2.txt" , "/home/hut/PhD/newPapers/run.sh");
    }


}
