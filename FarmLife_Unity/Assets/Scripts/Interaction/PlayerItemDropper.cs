using UnityEngine;

public class PlayerItemDropper : MonoBehaviour
{
    [SerializeField] private InventoryManager manager;
    [SerializeField] private GameObject defaultDropPrefab;
    [SerializeField] private float throwForce = 5f;

    private void Update()
    {
        if  (Input.GetKeyDown(KeyCode.Q))
        {
            DropSelectedItem();
        }
    }

    private void DropSelectedItem()
    {
        ItemStack stack = manager.GetSelectedStack();
        if (stack != null || stack.IsEmpty) return;

        GameObject prefabToDrop = stack.Data.WorldPrefab != null ? stack.Data.WorldPrefab : defaultDropPrefab;

        if (prefabToDrop == null)
        {
            Debug.LogError("Chưa gán Prefab cho vật phẩm này!");
            return;
        }

        Vector3 spawnPos = transform.position + Vector3.down * 0.5f;
        GameObject droppedObj = Instantiate(prefabToDrop, transform.position, Quaternion.identity);

        Collectable collectable = droppedObj.GetComponent<Collectable>();
        if (collectable != null)
        {
            collectable.Setup(stack.Data, 1);
        }

        Rigidbody2D rb = droppedObj.gameObject.GetComponent<Rigidbody2D>();
        if (rb != null)
        {
            Vector2 throwDir = transform.localScale.x > 0 ? Vector2.right : Vector2.left;
            rb.AddForce(new Vector2(throwDir.x * throwForce, throwForce), ForceMode2D.Impulse);
        }

        manager.ConsumeSelected(1);
    }
}
