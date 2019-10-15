package fitrozvrh.core.extract;

import fitrozvrh.FITrozvrh;
import fitrozvrh.core.data.Strings;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Kohout <xkohou14 at stud.fit.vutbr.cz>
 */
public class Downloader {

    private static boolean autheticatorSet = false;

    /**
     * Downloads file with authentication
     *
     * @param storeTo
     * @throws fitrozvrh.core.extract.DownloadException
     * @see
     * https://stackoverflow.com/questions/955624/download-a-file-from-the-internet-using-java-how-to-authenticate
     * code by Kairan at Stack Overflow:
     * https://stackoverflow.com/users/1342249/kairan
     * @param link
     * @return
     */
    public static File download(String link, String storeTo) throws DownloadException {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;

        if (!autheticatorSet) {
            // Install Authenticator
            createFolders();
            setKeystore();
            if(System.getProperty("login") == null) { // for testing set in Strings class
                FITAuthenticator.setPasswordAuthentication(Strings.LOGIN, Strings.PASSWORD);
            } else {
                FITAuthenticator.setPasswordAuthentication(System.getProperty("login"), System.getProperty("password"));
            }
            
            Authenticator.setDefault(new FITAuthenticator());
            autheticatorSet = true;
        }

        try {
            url = new URL(link);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(storeTo)));
            while ((line = br.readLine()) != null) {
                writer.write(line);
            }
            writer.close();

            File file = new File(storeTo);
            return file;
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            throw new DownloadException("MalformedURLException exception: " + mue.getMessage() + "\n" + mue.getStackTrace());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new DownloadException("IOException exception: " + ioe.getMessage() + "\n" + ioe.getStackTrace());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {

            }
        }
    }

    public static File downloadNoCertificate(String link, String storeTo) {
        try {
            URL website = new URL(link);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(storeTo);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new File(storeTo);
    }

    public static File downloadSubjectCard(String link) {
        //return new File("./test/subject-card.html");
        //return new File("./test/subject-card-SUI.html");
        return new File("./test/subject-card-UPA.html");
    }

    public static File downloadAcademicYear(String link) {
        return new File("./test/academicYear.html");
    }
    
    protected static void createFolders() {
        File f = new File(Strings.DOWNLOAD);
        if(!f.isDirectory()) {
            f.mkdir();
        }
    }

    /**
     * Sets keystore or create new one exemple from
     * https://coderanch.com/t/133048/engineering/programmatically-create-keystore-import-certificate
     * @throws fitrozvrh.core.extract.DownloadException
     */
    public static void setKeystore() throws DownloadException {
        File keyStore = new File(Strings.KEYSTORE);
        if (keyStore.exists()) {
            System.setProperty("javax.net.ssl.trustStore", Strings.KEYSTORE);
            return;
        }

        KeyStore ks;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(null, null);
            List<String> files = new ArrayList<>();
            files.add(Strings.FIT_CACERT);
            files.add(Strings.VUT_CACERT);
            for (String file : files) {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = null;
                while (bis.available() > 0) {
                    cert = cf.generateCertificate(bis);
                    ks.setCertificateEntry(file.substring(5), cert);
                }
                //ks.setCertificateEntry(file.substring(5), cert);
                ks.store(new FileOutputStream(Strings.KEYSTORE), "MyPass".toCharArray());
            }
            System.setProperty("javax.net.ssl.trustStore", Strings.KEYSTORE);
        } catch (KeyStoreException ex) {
            Logger.getLogger(FITrozvrh.class.getName()).log(Level.SEVERE, null, ex);
            throw new DownloadException("Keystore exception: " + ex.getMessage() + "\n" + ex.getStackTrace());
        } catch (IOException ex) {
            Logger.getLogger(FITrozvrh.class.getName()).log(Level.SEVERE, null, ex);
            throw new DownloadException("IOException exception: " + ex.getMessage() + "\n" + ex.getStackTrace());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FITrozvrh.class.getName()).log(Level.SEVERE, null, ex);
            throw new DownloadException("NoSuchAlgorithmException exception: " + ex.getMessage() + "\n" + ex.getStackTrace());
        } catch (java.security.cert.CertificateException ex) {
            Logger.getLogger(FITrozvrh.class.getName()).log(Level.SEVERE, null, ex);
            throw new DownloadException("CertificateException exception: " + ex.getMessage() + "\n" + ex.getStackTrace());
        }
    }
}
