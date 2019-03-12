package com.zhuocheng.subscribe;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class SubPubTest {
	public static void main(String[] args) throws ParseException {
//		IPublisher<String> publisher1 = new PublisherImpl<String>("发布者1");
//		ISubcriber<String> subcriber1 = new SubcriberImpl<String>("1", "up", "1", "");
//		ISubcriber<String> subcriber2 = new SubcriberImpl<String>("2", "down", "1", "");
//		subcriber1.subcribe(subscribePublish);
//		subcriber2.subcribe(subscribePublish);
//		publisher1.publish(subscribePublish, "welcome", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to2", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to3", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "to4", true, "up", "1", "", "");
//		publisher1.publish(subscribePublish, "yy", false, "up", "1", "", "");
		
		System.out.println(new Date().getTime());
		Set s = new LinkedHashSet<>();
		s.add(1);
		System.out.println(s.add(1));
	}
}
