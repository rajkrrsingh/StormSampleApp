package com.rajkrrsingh.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

/**
 * This is a basic example of a Storm topology.
 */

/**
 * This is a basic example of a storm topology.
 *
 * This topology demonstrates how to add three exclamation marks '!!!'
 * to each word emitted
 *
 * This is an example for Udacity Real Time Analytics Course - ud381
 *
 */
public class SampleTopology {

  public static class MyBolt extends BaseRichBolt
  {
    // To output tuples from this bolt to the next stage bolts, if any
    OutputCollector _collector;

    @Override
    public void prepare(
        Map                     map,
        TopologyContext         topologyContext,
        OutputCollector         collector)
    {
      // save the output collector for emitting tuples
      _collector = collector;
    }

    @Override
    public void execute(Tuple tuple)
    {
    	Integer inputNumber = tuple.getInteger(0);
    	_collector.emit(tuple,new Values(inputNumber * 2));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
      // tell storm the schema of the output tuple for this spout

      // tuple consists of a single column called 'exclamated-word'
      declarer.declare(new Fields("double-number"));
    }
  }
  
  public static class MySpout extends BaseRichSpout {
	  SpoutOutputCollector _collector;
	  Random _rand;


	  @Override
	  public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
	    _collector = collector;
	    _rand = new Random();
	  }

	  @Override
	  public void nextTuple() {
		  int nextRandomNumber = _rand.nextInt(100);
		  _collector.emit(new Values(nextRandomNumber));
	  }

	@Override
	public void declareOutputFields(OutputFieldsDeclarer paramOutputFieldsDeclarer) {
		// TODO Auto-generated method stub
		paramOutputFieldsDeclarer.declare(new Fields("double-number"));
	}

	 

	}

  public static void main(String[] args) throws Exception
  {
    // create the topology
    TopologyBuilder builder = new TopologyBuilder();

    // attach the word spout to the topology - parallelism of 10
    builder.setSpout("number", new MySpout(), 10);

    // attach the exclamation bolt to the topology - parallelism of 3
    builder.setBolt("double-number", new MyBolt(), 3).shuffleGrouping("number");

    // create the default config object
    Config conf = new Config();

    // set the config in debugging mode
    conf.setDebug(true);

    if (args != null && args.length > 0) {

      // run it in a live cluster

      // set the number of workers for running all spout and bolt tasks
      conf.setNumWorkers(3);

      // create the topology and submit with config
      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

    } else {

      // run it in a simulated local cluster

      // create the local cluster instance
      LocalCluster cluster = new LocalCluster();

      // submit the topology to the local cluster
      cluster.submitTopology("double-the-number", conf, builder.createTopology());

      // let the topology run for 30 seconds. note topologies never terminate!
      Thread.sleep(30000);

      // kill the topology
      cluster.killTopology("double-the-number");

      // we are done, so shutdown the local cluster
      cluster.shutdown();
    }
  }
}

