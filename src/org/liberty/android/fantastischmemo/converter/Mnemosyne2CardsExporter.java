/*
Copyright (C) 2013 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package org.liberty.android.fantastischmemo.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.liberty.android.fantastischmemo.AMEnv;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;
import org.liberty.android.fantastischmemo.dao.LearningDataDao;
import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;
import org.liberty.android.fantastischmemo.domain.LearningData;
import org.liberty.android.fantastischmemo.utils.AMFileUtil;
import org.liberty.android.fantastischmemo.utils.AMStringUtils;
import org.liberty.android.fantastischmemo.utils.AMZipUtils;

import com.google.common.base.Strings;
import com.google.inject.BindingAnnotation;

public class Mnemosyne2CardsExporter implements Converter {

    private static final long serialVersionUID = -8315483384166979473L;

    private final Random random = new Random();

    private AMFileUtil amFileUtil;

    @Inject
    public void setAmFileUtil(AMFileUtil amFileUtil) {
        this.amFileUtil = amFileUtil;
    }

    @Override
    public void convert(String src, String dest) throws Exception {
        // Make the tmp directory tmp/[src file name]/
        String srcFilename = FilenameUtils.getName(src);

        // This the file name for cards without extension
        String deckName = FilenameUtils.removeExtension(srcFilename);

        File tmpDirectory = new File(AMEnv.DEFAULT_TMP_PATH + deckName);

        FileUtils.deleteDirectory(tmpDirectory);
        FileUtils.forceMkdir(tmpDirectory);

        try {
            // Example content of cards
            // $ ls
            // METADATA  cards.xml  musicnotes

            // Make sure the XML file exists.
            File xmlFile = new File(tmpDirectory + "/cards.xml");

            // Before opening dest. Try to backup and delete the dest db.
            amFileUtil.deleteFileWithBackup(dest);
            createXMLFile(src, xmlFile);

            File metadataFile = new File(tmpDirectory + "/METADATA");
            createMetadata(deckName, metadataFile);


            // The last step is to see if there are images to export.
            File imageDir = new File(AMEnv.DEFAULT_IMAGE_PATH + srcFilename);
            if (imageDir.exists() && imageDir.isDirectory()) {
                // Copy all the images to the tmp directory
                Collection<File> imageFiles = FileUtils.listFiles(
                    imageDir,
                    new SuffixFileFilter(new String[] {"jpg", "png", "bmp"}, IOCase.INSENSITIVE),
                    DirectoryFileFilter.DIRECTORY);

                for (File f : imageFiles) {
                    FileUtils.copyFileToDirectory(f, tmpDirectory);
                }
            }

            // Now create the cards file:
            AMZipUtils.zipDirectory(tmpDirectory, "", new File(dest));

        } finally {
            FileUtils.deleteDirectory(tmpDirectory);
        }
    }

    private void createXMLFile(String dbPath, File xmlFile) throws IOException {
        AnyMemoDBOpenHelper helper = null;
        PrintWriter outXml = null;
        try {
            helper = AnyMemoDBOpenHelperManager.getHelper(dbPath);
            CardDao cardDao = helper.getCardDao();
            CategoryDao categoryDao = helper.getCategoryDao();
            LearningDataDao learningDataDao = helper.getLearningDataDao();
            int cardCount = (int) cardDao.countOf();

            outXml = new PrintWriter(new BufferedWriter(new FileWriter(xmlFile)));
            outXml.printf("<openSM2sync number_of_entries=\"%d\">\n", cardCount);
            // First card tags (categories)

            Map<String, String> categoryOidMap = new HashMap<String, String>();
            Map<Integer, String> cardIdOidMap = new HashMap<Integer, String>(cardCount * 4 / 3);
            Iterator<Category> categoryIterator = categoryDao.iterator();
            while (categoryIterator.hasNext()) {
                Category category = categoryIterator.next();
                String tagName = "__UNTAGGED__";
                String oId = generateOid();
                if (!Strings.isNullOrEmpty(category.getName())) {
                    tagName = category.getName();
                }
                categoryOidMap.put(tagName, oId);
                outXml.printf("<log type=\"10\" o_id=\"%s\"><name>%s</name></log>\n",
                        oId,
                        AMStringUtils.encodeXML(tagName));
            }
            // Then cards
            Iterator<Card> cardIterator = cardDao.iterator();
            while (cardIterator.hasNext()) {
                Card card = cardIterator.next();
                String front = card.getQuestion();
                String back = card.getAnswer();
                String oId = generateOid();
                cardIdOidMap.put(card.getId(), oId);
                outXml.printf("<log type=\"16\" o_id=\"%s\"><b>%s</b><f>%s</f></log>\n"
                        , oId
                        , AMStringUtils.encodeXML(back)
                        , AMStringUtils.encodeXML(front));
            }

            // Then learningData
            // <log card_t="1" fact_v="1.1" e="2.5" gr="-1" tags="5SfWDFGwqrlnGLDQxHHyG0" rt_rp_l="0" lps="0" l_rp="-1" n_rp="-1" ac_rp_l="0" rt_rp="0" ac_rp="0" type="6" o_id="7IXjCysHuCDtXo8hlFrK55" fact="7xmRCBH0WP0DZaxeFn5NLw"></log>
            Iterator<Card> cardIterator2 = cardDao.iterator();
            while (cardIterator2.hasNext()) {
                Card card = cardIterator2.next();
                categoryDao.refresh(card.getCategory());
                learningDataDao.refresh(card.getLearningData());
                String fact = cardIdOidMap.get(card.getId());
                String category = card.getCategory().getName();
                if (Strings.isNullOrEmpty(category)) {
                    category = "__UNTAGGED__";
                }
                String tags = categoryOidMap.get(category);
                String oId = generateOid();

                // Needs to converter to unix time
                LearningData learningData = card.getLearningData();
                long l_rp = learningData.getLastLearnDate().getTime() / 1000;
                long n_rp = learningData.getNextLearnDate().getTime() / 1000;
                outXml.printf("<log card_t=\"1\" fact_v=\"1.1\" e=\"%f\" gr=\"%d\" tags=\"%s\" rt_rp_l=\"%d\" lps=\"%d\" l_rp=\"%d\" n_rp=\"%d\" ac_rp_l=\"%d\" rt_rp=\"%d\" ac_rp=\"%d\" type=\"6\" o_id=\"%s\" fact=\"%s\"></log>\n"
                    , learningData.getEasiness()
                    , learningData.getGrade()
                    , tags
                    , learningData.getRetRepsSinceLapse()
                    , learningData.getLapses()
                    , l_rp
                    , n_rp
                    , learningData.getAcqRepsSinceLapse()
                    , learningData.getRetReps()
                    , learningData.getAcqReps()
                    , oId
                    , fact);
            }
            outXml.print("</openSM2sync>\n");
        } finally {
            if (helper != null) {
                AnyMemoDBOpenHelperManager.releaseHelper(helper);
            }
            if (outXml != null) {
                outXml.close();
            }
        }
    }

    private void createMetadata(String deckName, File metadataFile) throws IOException {
        PrintWriter outFile = null;
        try {
            outFile = new PrintWriter(new BufferedWriter(new FileWriter(metadataFile)));
            outFile.println("tags:");
            outFile.println("author_email:");
            outFile.println("notes:Imported from AnyMemo");
            outFile.println("author_name:");
            outFile.println("card_set_name:" + deckName);
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString  = formatter.format(new Date());
            outFile.println("date:" + dateString);
            outFile.print("revision:1");
        } finally {
            if (outFile != null) {
                outFile.close();
            }
        }
    }

    @Override
    public String getSrcExtension() {
        return "db";
    }

    @Override
    public String getDestExtension() {
        return "cards";
    }

    /**
     * Generate a random id for a card.
     *
     * @return random id.
     */
    private String generateOid() {
        String uuid = UUID.randomUUID().toString();
        String randomString = (uuid).replaceAll("-", "");
        return randomString.substring(0, 20);
    }

    @BindingAnnotation
    @Target({ ElementType. FIELD, ElementType.PARAMETER, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Type {};

}


