#!/bin/sh
gsutil mb -l eu gs://ai-worldpop
gsutil bucketpolicyonly set on gs://ai-worldpop
gsutil acl set public-read gs://ai-worldpop