package org.webharvest.runtime.processors.plugins.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.io.IOException;

/**
 * FTP processor
 */
public class FtpPlugin extends WebHarvestPlugin {

    FTPClient ftpClient;

    public String getName() {
        return "ftp";
    }

    public Variable executePlugin(Scraper scraper, DynamicScopeContext context) throws InterruptedException {
        String server = CommonUtil.nvl(evaluateAttribute("server", scraper), "");
        int port = evaluateAttributeAsInteger("port", 21, scraper);
        String username = CommonUtil.nvl(evaluateAttribute("username", scraper), "");
        String password = CommonUtil.nvl(evaluateAttribute("password", scraper), "");
        String account = CommonUtil.nvl(evaluateAttribute("account", scraper), "");
        String remoteDir = CommonUtil.nvl(evaluateAttribute("remotedir", scraper), "");

        setProperty("Server", server);
        setProperty("Port", port);
        setProperty("Username", username);
        setProperty("Password", password);
        setProperty("Account", account);
        setProperty("Remote Dir", remoteDir);

        ftpClient = new FTPClient();

        try {
            int reply;
            ftpClient.connect(server, port);
            reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new FtpPluginException("FTP server refused connection!");
            }

            if (CommonUtil.isEmptyString(account)) {
                ftpClient.login(username, password);
            } else {
                ftpClient.login(username, password, account);
            }

            if (!CommonUtil.isEmptyString(remoteDir)) {
                ftpClient.changeWorkingDirectory(remoteDir);
            }

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            return executeBody(scraper, scraper.getContext());
        } catch (IOException e) {
            throw new FtpPluginException(e);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                    scraper.getLogger().warn(ioe.getMessage(), ioe);
                }
            }
            ftpClient = null;
        }
    }

    public String[] getValidAttributes() {
        return new String[] {"server", "port", "username", "password", "account", "remotedir"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"server", "username", "password"};
    }

    public Class[] getDependantProcessors() {
        return new Class[] {
            FtpListPlugin.class,
            FtpGetPlugin.class,
            FtpPutPlugin.class,
            FtpDelPlugin.class,
            FtpMkdirPlugin.class,
            FtpRmdirPlugin.class
        };
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }
    
}