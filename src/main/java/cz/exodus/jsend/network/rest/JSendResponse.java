package cz.exodus.jsend.network.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JSendResponse<T> {
    private String status;
    private T data;

    public static JSendResponse success(Object data) {
        return new JSendResponse("success", data);
    }

    @Override
    public String toString() {
        return "JSendResponse{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
