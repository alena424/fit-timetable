package fitrozvrh;

import fitrozvrh.core.data.Strings;
import fitrozvrh.core.data.Subject;
import fitrozvrh.core.data.SubjectManager;
import fitrozvrh.core.extract.DownloadException;
import fitrozvrh.core.extract.Downloader;
import fitrozvrh.core.extract.Extractor;
import fitrozvrh.core.extract.FITAuthenticator;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.cert.CertificateException;

/**
 *
 * @author Petr Kohout <xkohou14 at stud.fit.vutbr.cz>
 */
public class FITrozvrh {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {            
            for(Subject s : SubjectManager.get().getSubjects()) { 
                System.out.println(s);
            }
        } catch (ParseException ex) {
            Logger.getLogger(FITrozvrh.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DownloadException ex) {
            Logger.getLogger(FITrozvrh.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
