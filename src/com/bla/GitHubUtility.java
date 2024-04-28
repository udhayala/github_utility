package com.bla;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class GitHubUtility {

	/**
	 * Function to create PR
	 * 
	 * @param username
	 * @param access token
	 * @param repositoryName
	 * @param mergeTitle
	 * @param head           fromBranch
	 * @param base           toBranch
	 * @param message
	 * @return pull request number from the response or -1 for API failure
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static int createPullRequest(
			String username, 
			String a, 
			String repositoryName, 
			String mergeTitle,
			String head, 
			String base, 
			String message
			) throws ClientProtocolException, IOException {
		try {
			// Hit the API end point to create PR
			// POST -> /repos/{owner}/{repo}/pulls
			CloseableHttpClient client = HttpClients.custom().build();
			HttpPost httpPost = new HttpPost(
					"https://api.github.com/repos/" + username + "/" + repositoryName + "/pulls");
			String json = "{" + 
					"\"title\":\"" + mergeTitle + "\"," + 
					"\"head\":\"" + head + "\"," + 
					"\"base\":\""+ base + "\"," + 
					"\"body\":\"" + message + "\"" +  
					"}";
			StringEntity stringEntity = new StringEntity(json);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
			httpPost.setHeader("X-GitHub-Api-Version", "2022-11-28");
			httpPost.setHeader("Accept", "application/vnd.github+json");
			httpPost.setHeader("Authorization", "Bearer " + a);
			httpPost.setEntity(stringEntity);
			HttpResponse response = client.execute(httpPost);

			// If API run successfully
			if (response.getStatusLine().getStatusCode() == 201) {
				System.out.println("-- PULL REQUEST CREATED SUCCESSFULLY --");
				String jsonResponseBody = EntityUtils.toString(response.getEntity());
				JSONObject jsonObject = new JSONObject(jsonResponseBody);
				int pr_number = (int) jsonObject.get("number");
				System.out.println("url : " + jsonObject.get("url"));
				System.out.println("id : " + jsonObject.get("id"));
				System.out.println("pull request number : " + pr_number);
				return pr_number;
			}
			// If API fails
			else {
				System.out.println("-- PULL REQUEST CREATION FAILED --");
				System.out.println("Response status : " + response.getStatusLine().getStatusCode());
				System.out.println("Status text : " + response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;

	}

	/**
	 * Function to merge a created PR
	 * 
	 * @param username
	 * @param access token
	 * @param repositoryName
	 * @param pr_number
	 * @param commitTitle
	 * @param commitMessage
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static void mergePullRequest(
			String username, 
			String a, 
			String repositoryName, 
			int pr_number,
			String commitTitle, 
			String commitMessage
			) throws ClientProtocolException, IOException {

		// Hit the API end point to merge
		// PUT -> /repos/{owner}/{repo}/pulls/{pull_number}/merge

		CloseableHttpClient client = HttpClients.custom().build();
		HttpPut httpPut = new HttpPut(
				"https://api.github.com/repos/" + username + "/" + repositoryName + "/pulls/" + pr_number + "/merge");
		httpPut.setHeader("X-GitHub-Api-Version", "2022-11-28");
		httpPut.setHeader("Accept", "application/vnd.github+json");
		httpPut.setHeader("Content-Type", "application/json");
		httpPut.setHeader("Authorization", "Bearer " + a);
		String json = "{" + 
				"\"commit_title\":\"" + commitTitle + "\"," + 
				"\"commit_message\":\"" + commitMessage + "\""
				+ "}";
		StringEntity stringEntity = new StringEntity(json);
		httpPut.setEntity(stringEntity);
		HttpResponse response = client.execute(httpPut);
		// If API runs successfully
		if (response.getStatusLine().getStatusCode() == 200) {
			System.out.println("-- PULL REQUEST MERGED SUCCESSFULLY --");
		}
		// If API fails
		else {
			System.out.println("-- MERGE FAILED. PLEASE FIX THE ISSUE AND RETRY --");
			System.out.println("Response status code : " + response.getStatusLine().getStatusCode());
			System.out.println("Status Text : " + response.getStatusLine().getReasonPhrase());
			String jsonResponseBody = EntityUtils.toString(response.getEntity());
			JSONObject jsonObject = new JSONObject(jsonResponseBody);
			System.out.println("Reason : " + jsonObject.get("message").toString());
		}

	}

	public static void main(String[] args) {
		System.out.println("Welcome to GitHub utility.");
		System.out.println();

		System.out.println("Let us create a pull request.");
		try (Scanner in = new Scanner(System.in)) {
			do {
				// CREATE PULL REQUEST
				// Input 1 : Username
				System.out.println("1. Please provide username:");
				String username = in.next();
				// Input 2 : Access Token
				System.out.println("2. Please provide access token:");
				String a = in.next();
				// Input 3 : Repository name
				System.out.println("3. Please provide repository name:");
				String repositoryName = in.next();
				// Input 4 : Merge title
				System.out.println("4. Please provide merge request title:");
				String mergeTitle = in.next();
				// Input 5 : From branch
				System.out.println("5. Please provide head branch name:");
				String head = in.next();
				// Input 6 : To branch
				System.out.println("6. Please provide base branch name:");
				String base = in.next();
				// Input 7 : Message
				System.out.println("7. Please provide message:");
				String message = in.next();

				int pr_number = createPullRequest(
						username, 
						a, 
						repositoryName, 
						mergeTitle, 
						head, 
						base,
						message);

				if (pr_number > -1) {
					System.out.println();
					// MERGE PULL REQUEST
					System.out.println("Do you want to merge the pull request? Y/N");

					if (in.next().equalsIgnoreCase("Y")) {
						// Input 1 : Commit title
						System.out.println("1. Please provide commit title:");
						String commitTitle = in.next();
						// Input 2 : Commit message
						System.out.println("2. Please provide commit message:");
						String commitMessage = in.next();
						mergePullRequest(
								username, 
								a, 
								repositoryName, 
								pr_number, 
								commitTitle, 
								commitMessage
								);
					}
				}
				System.out.println("\nDo you want to rerun the util?");
			}while(in.next().equalsIgnoreCase("Y"));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
