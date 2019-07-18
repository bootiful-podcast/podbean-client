package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.util.*;

@Log4j2
public class SimplePodbeanClient implements PodbeanClient {

	private final RestTemplate authenticatedRestTemplate;

	private final RestTemplate restTemplate = new RestTemplateBuilder().build();

	private final ObjectMapper objectMapper;

	private final ParameterizedTypeReference<Map<String, Collection<Podcast>>> getAllPodcastsTypeReference = new ParameterizedTypeReference<>() {
	};

	SimplePodbeanClient(RestTemplate authenticatedRestTemplate,
			ObjectMapper objectMapper) {
		this.authenticatedRestTemplate = authenticatedRestTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public Collection<Podcast> getAllPodcasts() {
		var responseEntity = this.authenticatedRestTemplate.exchange(
				"https://api.podbean.com/v1/podcasts", HttpMethod.GET, null,
				this.getAllPodcastsTypeReference);
		Assert.isTrue(responseEntity.getStatusCode().is2xxSuccessful(),
				"the result must be an HTTP 200-series");
		var entityBody = Objects.requireNonNull(responseEntity.getBody());
		return entityBody.getOrDefault("podcasts", Collections.emptyList());
	}

	@Override
	public UploadAuthorization upload(MediaType mediaType, File resource, long filesize) {
		var results = new ParameterizedTypeReference<UploadAuthorization>() {
		};
		var filename = Objects.requireNonNull(resource.getName());
		var uriString = UriComponentsBuilder
				.fromHttpUrl("https://api.podbean.com/v1/files/uploadAuthorize")
				.queryParam("content_type", mediaType.toString())//
				.queryParam("filename", filename)//
				.queryParam("filesize", filesize)//
				.build()//
				.toUriString();
		Assert.isTrue(resource.exists(), "the resource must point to a valid file");
		var responseEntity = this.authenticatedRestTemplate.exchange(uriString,
				HttpMethod.GET, null, results);
		var uploadAuthorization = responseEntity.getBody();
		log.info(uploadAuthorization);
		var presignedUrl = Objects.requireNonNull(uploadAuthorization).getPresignedUrl();
		var result = this.doUploadToS3(presignedUrl, resource);
		Assert.isTrue(result, "the result should be " + HttpStatus.OK.value());
		return uploadAuthorization;
	}

	@Override
	public Episode createEpisode(String title, String content, String status, String type,
			String mediaKey, String logoKey) {
		return null;
	}

	@Override
	@SneakyThrows
	public Collection<Episode> getEpisodes(int offset, int limit) {

		/*
		 * {"episodes":[{"id":"Z94JFB7A022","podcast_id":"o6DLxaF0purw",
		 * "title":"Podbean.com - superior podcast hosting."
		 * ,"content":"Welcome to Podbean.com. With Podbean, you can create professional podcasts in minutes without any programming knowledge. Our user-friendly interface allows you to upload, publish, manage and promote your podcasts with just a few clicks of your mouse. Just point, click and execute. How easy is that? Learn more at <a href=\"http:\/\/faq.podbean.com\">http:\/\/faq.podbean.com<\/a>. Have a question ? Check out the <a href=\"http:\/\/help.podbean.com\"> Podbean.com support center<\/a>. Happy Podcasting!"
		 * ,"logo":null,"media_url":"","player_url":null,"permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/podbean_best_podcast_hosting_audio_video_blog_hosting\/"
		 * ,"publish_time":1562927087,"status":"publish","type":"public","duration":null,
		 * "object":"Episode"},{"id":"ZNYC6B8247B","podcast_id":"o6DLxaF0purw",
		 * "title":"\"Mr. REST\" Mike Amundsen on APIs, Microservices, HTTP and more"
		 * ,"content":"Hi Spring fans! Welcome to another installment of a Bootiful Podcast! This week Josh Long (@starbuxman) talks to \"Mr. REST\" Mike Amundsen (@mamund) to talk about APIs, Microservices, HTTP and much more! \n\n* http:\/\/twitter.com\/mamund"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000565413707-dskfdm-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/4npdgw\/stream_650006849-a-bootiful-podcast-mr-rest-mike-amundsen-on-apis-microservices-http-and-more.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/znyc6-b8247b",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/mr-rest-mike-amundsen-on-apis-microservices-http-and-more\/"
		 * ,"publish_time":1562918820,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"GZ4EMB8247C","podcast_id":"o6DLxaF0purw",
		 * "title":"Dr. Venkat Subramaniam on Kotlin, Java, Spring, open-source, being productive, and so much more"
		 * ,"content":"Hi, Spring fans! In today's episode, I talk to the man, the myth, the legend, the good Dr. Venkat Subramaniam on Kotlin, the future of Java, Spring, open-source, being productive and awesome, and so much more. \n\n* Dr. Subramaniam on Twitter - http:\/\/twitter.com\/Venkat_S\n* https:\/\/twitter.com\/starbuxman\/status\/1103810728883040256 - a tweet asking that whatever recording I do with Dr. Subramaniam for the Bootiful Podcast be longer than an hour (wish granted!)\n* The image from today's episode comes from a joint-talk that the good Dr. Subramaniam and I did in Atlanta, GA for the epic Devnexus conference. I am using the photo from [Matt Raible's tweet](https:\/\/twitter.com\/mraible\/status\/1103692771028803590)\n* Dr. Subramaniam's new book Programming Kotlin: https:\/\/pragprog.com\/book\/vskotlin\/programming-kotlin"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000561798609-3g7jw0-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/i5gfbe\/stream_646455324-a-bootiful-podcast-dr-venkat-subramaniam-on-kotlin-java-spring-open-source-being-productive-and-so-much-more.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/gz4em-b8247c",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/dr-venkat-subramaniam-on-kotlin-java-spring-open-source-being-productive-and-so-much-more\/"
		 * ,"publish_time":1562294220,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"S59URB8247D","podcast_id":"o6DLxaF0purw",
		 * "title":"Datadog's Jason Yee on observability, operations, metrics, Kubernetes, language and more"
		 * ,"content":"Hi Spring fans! In this installment Josh Long (@starbuxman) talks to @Datadog 's Jason Yee (@gitbisect) about observability, operations, metrics, Kubernetes, language and more. \n\n* Jason on Twitter: http:\/\/twitter.com\/GitBisect\n* a great post on the (brief) history of Observability: https:\/\/www.cncf.io\/blog\/2019\/05\/21\/a-brief-history-of-opentelemetry-so-far\/\n* DevoxxUK: http:\/\/twitter.com\/DevoxxUK"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000558468936-8fqe6q-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/a2v4q8\/stream_643100952-a-bootiful-podcast-datadogs-jason-yee-on-observability-operations-metrics-kubernetes-language-and-more.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/s59ur-b8247d",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/datadogs-jason-yee-on-observability-operations-metrics-kubernetes-language-and-more\/"
		 * ,"publish_time":1561671180,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"RGYAKB8247F","podcast_id":"o6DLxaF0purw",
		 * "title":"IBM's Pratik Patel on function-as-a-service, Spring Cloud Function, Spring, Java and Devnexus"
		 * ,"content":"Hi Spring fans! This week Josh Long (@starbuxman) speaks to @IBM's Pratik Patel (@prpatel) about function-as-a-service, Spring Cloud Function, Spring, @Java and @Devnexus"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000555495309-9fg7qe-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/w77t6t\/stream_639959205-a-bootiful-podcast-ibms-pratik-patel-on-function-as-a-service-spring-cloud-function-spring-java-and-devnexus.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/rgyak-b8247f",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/ibms-pratik-patel-on-function-as-a-service-spring-cloud-function-spring-java-and-devnexus\/"
		 * ,"publish_time":1561101540,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"WJH7JB82480","podcast_id":"o6DLxaF0purw",
		 * "title":"Pivotal SVP Ian Andrews on our customers, new announcements, Kubernetes, and more"
		 * ,"content":"Hi Spring fans! In this installment Josh Long (@starbuxman) talks to Pivotal SVP Ian Andrews about Pivotal, Cloud Foundry, why he hasn't fired me yet, exciting new product announcements, frequent flyers, and more. \n\nIan on Twitter: http:\/\/twitter.com\/IanAndrewsDC"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000551748945-gasnkn-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/eqwt6v\/stream_636372984-a-bootiful-podcast-pivotal-svp-ian-andrews-on-our-customers-new-announcements-kubernetes-and-more.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/wjh7j-b82480",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/pivotal-svp-ian-andrews-on-our-customers-new-announcements-kubernetes-and-more\/"
		 * ,"publish_time":1560475440,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"SU4P2B82481","podcast_id":"o6DLxaF0purw",
		 * "title":"Spring Cloud lead Spencer Gibb"
		 * ,"content":"Hi Spring fans! In this installment Josh interviews Spring Cloud lead Spencer Gibb about open-source, Brazil, microservices, Spring, his journey to the Spring team, and more. \n\nTwitter:   http:\/\/Twittter.com\/SpencerBGibb"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000548064774-91wr9o-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/c9aj2d\/stream_632979783-a-bootiful-podcast-spring-cloud-lead-spencer-gibb.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/su4p2-b82481",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/spring-cloud-lead-spencer-gibb\/",
		 * "publish_time":1559876220,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"2G8F6B82482","podcast_id":"o6DLxaF0purw",
		 * "title":"Go-lang and Kubernetes legend Ver\u00f3nica Lopez"
		 * ,"content":"Hi Spring fans! In this episode I talk to Go-lang and Kubernetes-legend Ver\u00f3nica Lopez about community, physics, distributed systems and more. \n\nVer\u00f3nica on Twitter: http:\/\/twitter.com\/maria_fibonacci"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000544300443-hc858e-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/ag748m\/stream_629305281-a-bootiful-podcast-go-lang-and-kubernetes-legend-veronica-lopez.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/2g8f6-b82482",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/go-lang-and-kubernetes-legend-veronica-lopez\/"
		 * ,"publish_time":1559265420,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"GBETIB82483","podcast_id":"o6DLxaF0purw",
		 * "title":"Twitter's Chris Thalinger on Java, Graal, JVMs, JITs, Sao Paolo, Hawaii, and more"
		 * ,"content":"Hi Spring fans! In this installment of a Bootiful Podcast Josh Long (@starbuxman) talks to Twitter's Chris Thalinger (@christhalinger) about Graal VM; JITs; Compilers; Hawaii, USA; Sao Paolo, Brazil; and so much more.\n\nChris Thalinger on Twitter: @christhalinger\nGraalVM: https:\/\/www.graalvm.org\/"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/avatars-000517350273-cve1m4-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/59by5u\/stream_625772892-a-bootiful-podcast-twitters-chris-thalinger-on-java-graal-jvms-jits-sao-paolo-hawaii-and-more.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/gbeti-b82483",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/twitters-chris-thalinger-on-java-graal-jvms-jits-sao-paolo-hawaii-and-more\/"
		 * ,"publish_time":1558682940,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"5SDE9B82484","podcast_id":"o6DLxaF0purw",
		 * "title":"Nicolas Frankel on application security, integration testing, Kotlin and more"
		 * ,"content":"HI Spring fans! In this installment Josh Long (@starbuxman) talks to Nicolas Frankel (@nicolas_frankel) about integration testing, blogging, Kotlin, application security, living on the French\/Swiss border, blogging consistently, and much more. It's an interview with one of my favorite voices in the community.\n\nNicolas on Twitter: http:\/\/twitter.com\/nicolas_frankel\nNicolas' epic blog: https:\/\/blog.frankel.ch\/"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000536469108-vm50n7-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/pci25d\/stream_621942714-a-bootiful-podcast-nicolas-frankel-on-application-security-integration-testing-kotlin-and-more.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/5sde9-b82484",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/nicolas-frankel-on-application-security-integration-testing-kotlin-and-more\/"
		 * ,"publish_time":1558068420,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"DX3G6B82485","podcast_id":"o6DLxaF0purw",
		 * "title":"Kotlin team engineer Roman Elisarov"
		 * ,"content":"Hi Spring fans! In today's installment Josh Long (@starbuxman) sits down with Jetbrain's Roman Elizarov (@relizarov). Roman works on the Kotlin team and, among other things, focuses on asynchronous programming with things like coroutines. This interview was a very detailed dive into the opportunities for asynchronous programming for Spring developers using Kotlin, especially in light of the new coroutine support coming in Spring Framework 5.2.  \n\nTwitter: Roman Elizarov (@relizarov)\nA great blog by Sebastien Deleuze on our new support: https:\/\/spring.io\/blog\/2019\/04\/12\/going-reactive-with-spring-coroutines-and-kotlin-flow"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000532299114-1e7od7-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/igf6hw\/stream_618107604-a-bootiful-podcast-kotlin-team-engineer-roman-elisarov.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/dx3g6-b82485",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/kotlin-team-engineer-roman-elisarov\/",
		 * "publish_time":1557417120,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"KP49ZB82486","podcast_id":"o6DLxaF0purw",
		 * "title":"Spring Cloud Engineer Olga Maciaszek-Sharma"
		 * ,"content":"Hi Spring fans! In this installment Josh Long talks to Spring Cloud engineeer Olga Maciaszek-Sharma about joining the Sping team, testing, consumer driven contracts and consumer driven contract testing, and so much more. \n\nOlga's Twitter: http:\/\/twitter.com\/olga_maciaszek"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000529379997-co64vc-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/cfa7vj\/stream_615074445-a-bootiful-podcast-spring-cloud-engineer-olga-maciaszek-sharma.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/kp49z-b82486",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/spring-cloud-engineer-olga-maciaszek-sharma\/"
		 * ,"publish_time":1556877240,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"BS5U8B82488","podcast_id":"o6DLxaF0purw",
		 * "title":"Spring Batch and Spring Cloud Task lead Michael Minella"
		 * ,"content":"Hi Spring fans! In this episode we talk to Spring Batch and Spring Cloud Task lead Michael Minella about all things task and batch processing with Spring, South Africa, and so much more. \n\nMichael Minella on Twitter: http:\/\/twitter.com\/michaelminella"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/avatars-000517350273-cve1m4-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/ma5ydv\/stream_611519967-a-bootiful-podcast-spring-batch-and-spring-cloud-task-lead-michael-minella.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/bs5u8-b82488",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/spring-batch-and-spring-cloud-task-lead-michael-minella\/"
		 * ,"publish_time":1556239860,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"HSRU8B8248A","podcast_id":"o6DLxaF0purw",
		 * "title":"Pivotal Field CTO and OG Cloud Native Matt Stine on Architecture"
		 * ,"content":"Hi Spring fans! Welcome to another installment of a Bootiful Podcast. In this episode, recorded in lovely Johannesburg, South Africa for the SpringOne Tour event, I'm joined by my buddy and one of the original cloud natives, Matt Stine!\n\n\nMatt's twitter: http:\/\/twitter.com\/mstine"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000522001251-0xqqkm-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/jzqgcm\/stream_607633950-a-bootiful-podcast-matt-stine-april-2019-mixdown.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/hsru8-b8248a",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/pivotal-field-cto-and-og-cloud-native-matt-stine-on-architecture\/"
		 * ,"publish_time":1555632000,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"MTR83B8248B","podcast_id":"o6DLxaF0purw",
		 * "title":"Rabobank's Roy Braam"
		 * ,"content":"Hi Spring fans! In this installment Josh Long talks to Roy Braam, a solutions architect at Rabobank, a bank in the Netherlands, about how they are able to quickly iterate despite regulation and size. \n\nRoy Braam on Twitter: http:\/\/twitter.com\/rbraam\nRabobank: https:\/\/www.rabobank.com\/en\/home\/index.html\nJosh Long on Twitter: http:\/\/twitter.com\/starbuxman"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/avatars-000517350273-cve1m4-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/223jkr\/stream_604685703-a-bootiful-podcast-rabobanks-roy-braam.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/mtr83-b8248b",
		 * "permalink_url":"https:\/\/starbuxman.podbean.com\/e\/rabobanks-roy-braam\/",
		 * "publish_time":1555025400,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"TBECAB8248C","podcast_id":"o6DLxaF0purw",
		 * "title":"Kylie Liang and Theresa Nguyen on Microsoft's Java and Spring integrations"
		 * ,"content":"Hi Spring fans! In this week's episode Josh talks to Microsoft's Kylie Liang and Theresa Nguyen about the Microsoft Java and Spring integrations, the Java ecosystem, open-source, and more. \n\nKylie (the right most person in the photo) on Twitter: https:\/\/twitter.com\/liangkylie\n\nTheresa (the left most person in the photo) on Twitter: http:\/\/twitter.com\/RockClimberT\n\nJosh on Twitter: http:\/\/twitter.com\/starbuxman\n\nThe \"Spring Tips\" screencast introducing the Spring and Microsoft Azure integration: https:\/\/www.youtube.com\/watch?v=jRgZe7kWDnU"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000514895715-cxxk7h-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/u6fqg7\/stream_600772455-a-bootiful-podcast-kylie-liang-and-theresa-nguyen-on-microsofts-java-and-spring-integrations.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/tbeca-b8248c",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/kylie-liang-and-theresa-nguyen-on-microsofts-java-and-spring-integrations\/"
		 * ,"publish_time":1554372420,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"G5JG5B8248D","podcast_id":"o6DLxaF0purw",
		 * "title":"Josh Mckenty - \"Better Josh\" - on data sovereignty, Python, the cloud, Pivotal, and more"
		 * ,"content":"Hi Spring fans! In this week's installment of a Bootiful Podcast I, Josh Long, welcome Josh Mckenty - or \"better Josh,\" as I affectionately call him - to the show to discuss Pivotal, Cloud Foundry, Python, microservices, and data sovereignty, among other things. \n\nJosh Mckenty on Twitter: http:\/\/twitter.com\/jmckenty"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000511904028-l2rqq3-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/5tkqad\/stream_597710070-a-bootiful-podcast-josh-mckenty-better-josh-on-data-sovereignty-python-the-cloud-pivotal-and-more.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/g5jg5-b8248d",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/josh-mckenty-better-josh-on-data-sovereignty-python-the-cloud-pivotal-and-more\/"
		 * ,"publish_time":1553837280,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"3UJWYB8248E","podcast_id":"o6DLxaF0purw",
		 * "title":"CQRS with AxonIQ's Steven van Beelen and Pivotal's Ben Wilcock"
		 * ,"content":"Hi Spring fans! In this week's installment Josh Long talks to AxonIQ's Steven van Beelen, lead of the Axon project, and Pivotal's Ben Wilcock, on CQRS, event-sourcing, event-storming, microservices, Spring Boot and the long camaraderie  shared by the Spring and Axon teams\n\nSteve van Beelen: https:\/\/twitter.com\/smcvbeelen \nBen Wilcock: http:\/\/twitter.com\/benbravo73\nAllard Buijze: http:\/\/twitter.com\/allardbz\nAxonIQ: https:\/\/axoniq.io\/"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000507510153-632a0p-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/m2qcmh\/stream_593419098-a-bootiful-podcast-cqrs-with-axoniqs-steven-van-beelen-and-pivotals-ben-wilcock.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/3ujwy-b8248e",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/cqrs-with-axoniqs-steven-van-beelen-and-pivotals-ben-wilcock\/"
		 * ,"publish_time":1553154780,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"JQZNIB8248F","podcast_id":"o6DLxaF0purw",
		 * "title":"Joe Grandja on Spring Security 5's OAuth Support"
		 * ,"content":"Hi Spring fans! Welcome to another installment of a Bootiful Podcast! This week @starbuxman talks to Joe Grandja who, not coincidentally, just celebrated his third anniversary working on the Spring team!  Joe has been instrumental in building Spring Security 5.x and its OAuth client and resource-server support. \n\nJoe's team page: https:\/\/spring.io\/team\/pivotal-joe-grandja \nJoe on Twitter: https:\/\/twitter.com\/joe_grandja"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000503926509-qd9n7y-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/zkf6eq\/stream_589977558-a-bootiful-podcast-joe-grandja-on-spring-security-5s-oauth-support.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/jqzni-b8248f",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/joe-grandja-on-spring-security-5s-oauth-support\/"
		 * ,"publish_time":1552604400,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"},{"id":"HAZ4AB82490","podcast_id":"o6DLxaF0purw",
		 * "title":"Matt Raible and James Ward at Devnexus 2019"
		 * ,"content":"Hi Spring fans! In this extra-long installment I talk with longtime friends and fellow developer advocates, Okta's Matt Raible and Google's James Ward. We talked about Java, Kotlin, cloud computing technologies, security, Go, paradigm changes, web frameworks past and present, Macromedia, Scala, and a MILLION more things! This was a ton of fun for me so I'm hoping you'll enjoy it too. \n\nMatt on Twitter: http:\/\/twitter.com\/mraible\nJames on Twitter: http:\/\/twitter.com\/_jamesward"
		 * ,"logo":
		 * "https:\/\/pbcdn1.podbean.com\/imglogo\/ep-logo\/pbblog5518947\/artworks-000500585115-vxz1eq-original.jpg"
		 * ,"media_url":
		 * "https:\/\/starbuxman.podbean.com\/mf\/play\/kdsxtw\/stream_586589739-a-bootiful-podcast-matt-raible-and-james-ward-at-devnexus-2019.mp3"
		 * ,"player_url":"https:\/\/www.podbean.com\/media\/player\/haz4a-b82490",
		 * "permalink_url":
		 * "https:\/\/starbuxman.podbean.com\/e\/matt-raible-and-james-ward-at-devnexus-2019\/"
		 * ,"publish_time":1551998340,"status":"publish","type":"public","duration":0,
		 * "object":"Episode"}],"offset":0,"limit":20,"has_more":true,"count":"29"}
		 */

		var ptr = new ParameterizedTypeReference<Map<String, Object>>() {
		};
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl("https://api.podbean.com/v1/episodes");

		if (offset > 0)
			builder.queryParam("offset", offset);
		if (limit > 0)
			builder.queryParam("limit", limit);

		var url = builder.build().toUriString();
		var responseEntity = this.authenticatedRestTemplate.exchange(url, HttpMethod.GET,
				null, String.class);

		var json = responseEntity.getBody();
		var jsonNode = this.objectMapper.readTree(json);
		JsonNode episodes = jsonNode.get("episodes");
		return objectMapper.readValue(episodes.toString(),
				new TypeReference<Collection<Episode>>() {
				});

	}

	@Override
	public Collection<Episode> getEpisodes() {
		return getEpisodes(0, 0);
	}

	@SneakyThrows
	private boolean doUploadToS3(String presignedUrl, File file) {
		var url = URI.create(presignedUrl);
		var request = RequestEntity.put(url)
				.contentType(MediaType.parseMediaType("audio/mpeg"))
				.body(new FileSystemResource(file));
		return this.restTemplate.exchange(url, HttpMethod.PUT, request, String.class)
				.getStatusCode().is2xxSuccessful();
	}

}
