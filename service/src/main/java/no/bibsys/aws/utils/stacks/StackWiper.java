package no.bibsys.aws.utils.stacks;

public interface StackWiper {

    void wipeStacks();

    void emptyAndDeleteBucket(String bucketName);
}
