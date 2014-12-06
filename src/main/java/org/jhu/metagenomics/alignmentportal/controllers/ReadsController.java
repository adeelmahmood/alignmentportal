package org.jhu.metagenomics.alignmentportal.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.ReadGroupSet;
import com.google.api.services.genomics.model.SearchReadsRequest;
import com.google.api.services.genomics.model.SearchReadsResponse;

@RestController
@RequestMapping("/reads")
public class ReadsController {

	private static final int PAGE_SIZE = 10;

	@Value("${google.genomics.project.number}")
	private long projectNumber;

	private final Genomics genomics;

	@Autowired
	public ReadsController(Genomics genomics) {
		this.genomics = genomics;
	}

	@RequestMapping(value = "/list/{readGroupSetId}", method = RequestMethod.GET)
	public Map<String, Object> list(@PathVariable String readGroupSetId,
			@RequestParam(required = false) Long start,
			@RequestParam(required = false) Long end,
			@RequestParam(required = false) String referenceName,
			@RequestParam(required = false) String nextPageToken)
					throws IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		
		//get read group set
		ReadGroupSet readGroupSet = genomics.readgroupsets().get(readGroupSetId).execute();
		data.put("readGroupSetId", readGroupSet.getId());
		data.put("readGroupSetName", readGroupSet.getName());
		data.put("readGroupSetFilename", readGroupSet.getFilename());
		
		//get reads
		SearchReadsRequest req = new SearchReadsRequest()
								.setReadGroupSetIds(Arrays.asList(readGroupSetId))
								.setPageSize(PAGE_SIZE)
								.setReferenceName(referenceName)
								.setPageToken(nextPageToken);
		if(start != null) {
			req.setStart(start);
		}
		if(end != null) {
			req.setEnd(end);
		}
		SearchReadsResponse resp = genomics.reads().search(req).execute();
		data.put("reads", resp.getAlignments());
		data.put("previousPageToken", nextPageToken);
		data.put("nextPageToken", resp.getNextPageToken());
		return data;
	}
}