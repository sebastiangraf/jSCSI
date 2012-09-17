package org.jscsi.target.connection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.connection.stage.TargetStage;
import org.jscsi.target.connection.stage.fullfeature.FormatUnitStage;
import org.jscsi.target.connection.stage.fullfeature.InquiryStage;
import org.jscsi.target.connection.stage.fullfeature.ReportLunsStage;
import org.jscsi.target.connection.stage.fullfeature.RequestSenseStage;
import org.jscsi.target.connection.stage.fullfeature.SendDiagnosticStage;
import org.jscsi.target.connection.stage.fullfeature.TestUnitReadyStage;
import org.jscsi.target.connection.stage.fullfeature.TextNegotiationStage;
import org.jscsi.target.connection.stage.fullfeature.UnsupportedOpCodeStage;
import org.jscsi.target.settings.SettingsException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConnectionTest {
    // /**
    // * This test executes the mode sense stage covering the stage execution
    // * using a the target connection and a stub'ish pdu.
    // */
    // @Test(enabled = false)
    // public void testModeSenseStage() throws Exception {
    // TargetConnection connection = targetServer.getConnection();
    // modeSenseStage = new ModeSenseStage(new TargetFullFeaturePhase(connection));
    //
    // final ProtocolDataUnit pdu =
    // new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");
    //
    // modeSenseStage.execute(pdu);
    // }
    //
    // /**
    // * This test executes the ping stage covering the stage execution
    // * using a the target connection and a stub'ish pdu.
    // */
    // @Test(enabled = false)
    // public void testPingStage() throws Exception {
    // TargetConnection connection = targetServer.getConnection();
    // pingStage = new PingStage(new TargetFullFeaturePhase(connection));
    //
    // final ProtocolDataUnit pdu =
    // new ProtocolDataUnitFactory().create(false, true, OperationCode.NOP_OUT, "None", "None");
    //
    // pingStage.execute(pdu);
    // }

    @Test(dataProvider = "instantiateStages")
    public void testStages(Class<TargetStage> pTargetStageClass, TargetStage[] pStages,
        Class<ProtocolDataUnit> pDataUnitClass, ProtocolDataUnit[] pDataUnits, Class<Checker> pCheckerClass,
        Checker[] pChecker) throws DigestException, IOException, InterruptedException, InternetSCSIException,
        SettingsException {
        assertEquals(pStages.length, pDataUnits.length);
        assertEquals(pStages.length, pChecker.length);
        for (int i = 0; i < pStages.length; i++) {
            pStages[i].execute(pDataUnits[i]);
            pChecker[i].check(pStages[i].getConnection());
        }

    }

    @DataProvider(name = "instantiateStages")
    public Object[][] provideStages() {
        Connection connection = mock(Connection.class);
        TargetFullFeaturePhase phase = new TargetFullFeaturePhase(connection);
        Object[][] returnVal =
            {
                {
                    TargetStage.class,
                    new TargetStage[] {
                        new TestUnitReadyStage(phase), new SendDiagnosticStage(phase),
                        new ReportLunsStage(phase), new InquiryStage(phase), new RequestSenseStage(phase),
                        new TextNegotiationStage(phase), new UnsupportedOpCodeStage(phase),
                        new FormatUnitStage(phase)
                    },
                    ProtocolDataUnit.class,
                    new ProtocolDataUnit[] {
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST,
                            "None", "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST,
                            "None", "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None")
                    }, Checker.class, new Checker[] {
                        // TestUnitReadyStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }, // SendDiagnosticStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }, // ReportLunsStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }, // InquiryStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }, // RequestSenseStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }, // TextNegotiationStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }, // UnsupportedOpCodeStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }, // FormatUnitStage checker
                        new Checker() {

                            @Override
                            public void check(final Connection pConnection) throws DigestException,
                                InternetSCSIException, IOException, SettingsException {
                                when(pConnection.receivePdu()).thenReturn(null);
                            }
                        }
                    }
                }
            };
        return returnVal;
    }

    static interface Checker {
        void check(final Connection pConnection) throws DigestException, InternetSCSIException, IOException,
            SettingsException;
    }

}
