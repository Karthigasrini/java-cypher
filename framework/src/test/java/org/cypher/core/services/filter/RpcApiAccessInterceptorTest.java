package org.cypher.core.services.filter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cypher.api.GrpcAPI;
import org.cypher.api.WalletGrpc;
import org.cypher.api.WalletSolidityGrpc;
import org.cypher.common.application.Application;
import org.cypher.common.application.ApplicationFactory;
import org.cypher.common.application.CypherApplicationContext;
import org.cypher.common.utils.FileUtil;
import org.cypher.core.Constant;
import org.cypher.core.config.DefaultConfig;
import org.cypher.core.config.args.Args;
import org.cypher.core.services.RpcApiService;
import org.cypher.core.services.interfaceOnPBFT.RpcApiServiceOnPBFT;
import org.cypher.core.services.interfaceOnSolidity.RpcApiServiceOnSolidity;

public class RpcApiAccessInterceptorTest {

  private static final Logger logger = LoggerFactory.getLogger("Test");

  private static CypherApplicationContext context;

  private static WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private static WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private static WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubPBFT = null;
  private static Application appTest;

  private static String dbPath = "output_rpc_api_access_filter_test";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * init logic.
   */
  @BeforeClass
  public static void init() {
    Args.setParam(new String[] {"-d", dbPath}, Constant.TEST_CONF);
    String fullNode = String.format("%s:%d", Args.getInstance().getNodeDiscoveryBindIp(),
        Args.getInstance().getRpcPort());
    String solidityNode = String.format("%s:%d", Args.getInstance().getNodeDiscoveryBindIp(),
        Args.getInstance().getRpcOnSolidityPort());
    String pBFTNode = String.format("%s:%d", Args.getInstance().getNodeDiscoveryBindIp(),
        Args.getInstance().getRpcOnPBFTPort());

    ManagedChannel channelFull = ManagedChannelBuilder.forTarget(fullNode)
        .usePlaintext()
        .build();
    ManagedChannel channelpBFT = ManagedChannelBuilder.forTarget(pBFTNode)
        .usePlaintext()
        .build();
    ManagedChannel channelSolidity = ManagedChannelBuilder.forTarget(solidityNode)
        .usePlaintext()
        .build();

    context = new CypherApplicationContext(DefaultConfig.class);

    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelSolidity);
    blockingStubPBFT = WalletSolidityGrpc.newBlockingStub(channelpBFT);

    RpcApiService rpcApiService = context.getBean(RpcApiService.class);
    RpcApiServiceOnSolidity rpcApiServiceOnSolidity =
        context.getBean(RpcApiServiceOnSolidity.class);
    RpcApiServiceOnPBFT rpcApiServiceOnPBFT = context.getBean(RpcApiServiceOnPBFT.class);

    appTest = ApplicationFactory.create(context);
    appTest.addService(rpcApiService);
    appTest.addService(rpcApiServiceOnSolidity);
    appTest.addService(rpcApiServiceOnPBFT);
    appTest.initServices(Args.getInstance());
    appTest.startServices();
    appTest.startup();
  }

  /**
   * destroy the context.
   */
  @AfterClass
  public static void destroy() {
    Args.clearParam();
    appTest.shutdownServices();
    appTest.shutdown();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }

  @Test
  public void testAccessDisabledFullNode() {
    List<String> disabledApiList = new ArrayList<>();
    disabledApiList.add("getaccount");
    disabledApiList.add("getblockbynum");
    Args.getInstance().setDisabledApiList(disabledApiList);

    final GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    thrown.expect(StatusRuntimeException.class);
    thrown.expectMessage("this API is unavailable due to config");
    blockingStubFull.getBlockByNum(message);
  }

  @Test
  public void testAccessDisabledSolidityNode() {
    List<String> disabledApiList = new ArrayList<>();
    disabledApiList.add("getaccount");
    disabledApiList.add("getblockbynum");
    Args.getInstance().setDisabledApiList(disabledApiList);

    final GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    thrown.expect(StatusRuntimeException.class);
    thrown.expectMessage("this API is unavailable due to config");
    blockingStubSolidity.getBlockByNum(message);
  }

  @Test
  public void testAccessDisabledPBFTNode() {
    List<String> disabledApiList = new ArrayList<>();
    disabledApiList.add("getaccount");
    disabledApiList.add("getblockbynum");
    Args.getInstance().setDisabledApiList(disabledApiList);

    final GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    thrown.expect(StatusRuntimeException.class);
    thrown.expectMessage("this API is unavailable due to config");
    blockingStubPBFT.getBlockByNum(message);
  }

  @Test
  public void testAccessNoDisabled() {
    Args.getInstance().setDisabledApiList(Collections.emptyList());

    final GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    Assert.assertNotNull(blockingStubFull.getBlockByNum(message));
    Assert.assertNotNull(blockingStubSolidity.getBlockByNum(message));
    Assert.assertNotNull(blockingStubPBFT.getBlockByNum(message));
  }

  @Test
  public void testAccessDisabledNotIncluded() {
    List<String> disabledApiList = new ArrayList<>();
    disabledApiList.add("getaccount");
    Args.getInstance().setDisabledApiList(disabledApiList);

    final GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    Assert.assertNotNull(blockingStubFull.getBlockByNum(message));
    Assert.assertNotNull(blockingStubSolidity.getBlockByNum(message));
    Assert.assertNotNull(blockingStubPBFT.getBlockByNum(message));
  }

}

