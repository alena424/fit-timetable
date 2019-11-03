package com.tam.fittimetable.backend.core.extract;

import com.tam.fittimetable.backend.core.data.Strings;
import com.tam.fittimetable.backend.core.data.SubjectManager;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Extracts Subjects from HTML file.
 *
 * @author Petr Kohout <xkohou14 at stud.fit.vutbr.cz>
 */
public class Extractor {

    private File file;

    public Extractor(File file) {
        this.file = file;
    }

    /**
     * Parse file and creates records in SubjectManager
     */
    public void parse() {
        try {
            String day, name, room, color, subjectLink;
            int from, to = 7, colspan;

            int rowCounter = 2; //begins on second row
            int rowsToAnotherDay = 0;

            Document doc = Jsoup.parse(file, "ISO-8859-2");
            Element elm = doc.select("table").get(3); // select 3rd table which contains timetable
            SubjectManager manager = SubjectManager.get();
            Element dayEl;

            for (int y = 0; y < 5; y++) { //iterate through week days
                if (elm.select("tr").size() <= rowCounter) {
                    System.out.println("Konec tabulky " + elm.select("tr") );
                    continue;
                }
                dayEl = elm.select("tr").get(rowCounter);
                day = dayEl.select("th").first().text(); // select day name // Monday, Tuesday...
                rowsToAnotherDay = Integer.parseInt(dayEl.select("th").first().attr("rowspan")); // selects day rowspan which indicates how many rows day contains

                for (int i = 0; i < rowsToAnotherDay; i++) { //iterate through rows to select subjects
                    dayEl = elm.select("tr").get(rowCounter + i);
                    from = 7; //every day starts at 7 o'clock
                    to = 7; //every day starts at 7 o'clock
                    for (Element e : dayEl.select("td")) {
                        if (e.hasAttr("colspan")) {
                            colspan = Integer.parseInt(e.attr("colspan"));
                        } else {
                            colspan = 1;
                        }
                        to += colspan;
                        if (!e.hasAttr("bgcolor")) { // if does not "bgcolor" it is space because there is no subject
                            from += colspan;
                            continue;
                        }
                        color = e.attr("bgcolor");
                        subjectLink = e.select("a").first().attr("href");
                        name = e.select("b").first().text();
                        room = e.select("a").get(1).text();

                        //System.out.println(name + "|" + subjectLink + "|" + room + "|" + from + "|" + to + "|" + day + "|" + color + "|--> " + manager.getSubjects().size());
                        manager.addSubject(name, getLinkToSubjectCard(subjectLink), room, from, to, day, color);
                        manager.getSubjects().get(manager.getSubjects().size() - 1).setWeeksOfMentoring();
                        from += colspan;
                    }
                }
                rowCounter += rowsToAnotherDay; // skip to another day
            }
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param link
     * @return
     */
    private String getLinkToSubjectCard(String link) throws IOException {
        Document doc = Jsoup.parse(Downloader.download(Strings.WEB_PREFIX + link, Strings.SUBJECT_PRIVATE_FILE), "ISO-8859-2");
        Elements elms = doc.select("a"); // select a elemnts

        for (Element e : elms) {
            if (e.text().toLowerCase().contains("web")) {
                return e.attr("href");
            }
        }

        throw new IOException("address: " + link + " does not contain subject link");
    }

    public static List<Date> selectDatesOfSemesters() throws ParseException {
        try {
            List<Date> dates = new ArrayList<Date>();
            SimpleDateFormat formatter = new SimpleDateFormat(Strings.DATE_FORMAT_YYYY_MM_DD);

            Document doc;
            doc = Jsoup.parse(Downloader.download(Strings.ACADEMIC_YEAR, Strings.ACADEMIC_YEAR_FILE), "ISO-8859-2");
            Elements elms = doc.select("span"); // select spans which contain <times> tags

            for (Element e : elms) {
                String text = e.text().toLowerCase();
                if (text.contains("-") && e.select("time").size() == 2) {
                    // span with dates of semester looks like...
                    //<span class="c-schedule__time font-secondary">
                    //    <time datetime="2019-09-23">23. September 2019</time>
                    //    -
                    //    <time datetime="2019-12-20">20. December 2019</time>
                    //</span>
                    //but there are another blocks which pass (winter holiday...)
                    Date d1 = formatter.parse(e.select("time").get(0).attr("datetime"));
                    Date d2 = formatter.parse(e.select("time").get(1).attr("datetime"));

                    long diffInMillies = Math.abs(d2.getTime() - d1.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    if (diff >= 12 * 7) { // there are more then 10 weeks (semester has 13 but there are not so long holidays :D )
                        dates.add(d1);
                        dates.add(d2);
                        System.out.println("Dates of semester extracted: " + d1 + " - " + d2);
                    }
                }
            }
            if (dates.isEmpty()) {
                throw new ParseException("Dates of semester are not recognized in source file.", 0);
            }
            return dates;
        } catch (IOException ex) {
            Logger.getLogger(Extractor.class.getName()).log(Level.SEVERE, null, ex);
            throw new ParseException("Dates of semester are not recognized.", 0);
        }
    }
}
