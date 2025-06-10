package dto;

import lombok.Data;

@Data
public class TextInterval {
    private int start;
    private int end;
    private int size;

    public TextInterval(int start, int len) {
        this.start = start;
        this.end = start + len;
        this.size = len;
    }

    public void addToEnd(int val) {
        end = end + val;
        size = end - start;
    }

    public void shrinkFromStart(int val) {
        start = start + val;
        size = end - start;
    }

    public Boolean checkIntersect(TextInterval interval) {
        return !(end < interval.getStart() || start > interval.getEnd());
    }

    public Boolean unityIntervals(TextInterval interval) {
        if (!checkIntersect(interval)) {
            return false;
        }
        if (start > interval.getStart()) {
            start = interval.getStart();
        }
        if (end < interval.getEnd()) {
            end = interval.getEnd();
        }
        size = end - start;
        return true;
    }
}
