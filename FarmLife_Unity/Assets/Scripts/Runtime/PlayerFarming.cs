using UnityEngine;

public class PlayerFarming : MonoBehaviour
{

    [Header("Database")]
    [SerializeField] private CropDatabase cropDatabase;

    private void Update()
    {
        if (Input.GetMouseButtonDown(0))
        {
            TryInteractWithFarmLand();
        }
    }

    private void TryInteractWithFarmLand()
    {
        Vector2 mousePos = Camera.main.ScreenToWorldPoint(Input.mousePosition);
        RaycastHit2D hit = Physics2D.Raycast(mousePos, Vector2.zero);

        if (hit.collider != null)
        {
            Farmland farm = hit.collider.GetComponent<Farmland>();
            if (farm == null) return;

            ItemStack heldItem = InventoryManager.Instance.GetSelectedStack();
            if (heldItem == null || heldItem.IsEmpty) return;

            ItemData itemData = heldItem.Data;

            if (itemData.Type == ItemType.Consumable)
            {
                CropData crop = GetCropDataFromSeed(itemData);

                if (crop != null && !farm.IsOccupied)
                {
                    farm.PlantSeed(crop);
                    InventoryManager.Instance.ConsumeSelected(1);
                }
                else if (crop == null)
                {
                    Debug.LogWarning($"Hạt giống {itemData.ItemName} chưa có trong Database!");
                }
            }
            else if (farm.IsMature)
            {
                farm.Harvest();
            }
        }
    }

    private CropData GetCropDataFromSeed(ItemData seed)
    {
        if (cropDatabase == null)
        {
            Debug.LogError("LỖI: Chưa gán CropDatabase vào PlayerFarming!");
            return null;
        }

        return cropDatabase.GetCropBySeed(seed);
    }
}
