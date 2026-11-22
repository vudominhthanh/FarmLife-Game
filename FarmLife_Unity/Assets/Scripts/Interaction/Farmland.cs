using UnityEngine;

public class Farmland : MonoBehaviour
{
    private SpriteRenderer spriteRenderer;
    private CropData currentCrop;
    private int currentStage = 0;
    private float growthTimer = 0f;

    public bool IsMature => currentCrop != null && currentStage >= currentCrop.MaxStages - 1;

    public bool IsOccupied => currentCrop != null;

    private void Awake()
    {
        spriteRenderer = GetComponent<SpriteRenderer>();
    }

    private void Update()
    {
        if (currentCrop != null && currentStage < currentCrop.MaxStages - 1)
        {
            growthTimer += Time.deltaTime;
            if (growthTimer >= currentCrop.timePerStage)
            {
                growthTimer = 0f;
                currentStage++;
                UpdateSprite();
            }
        }
    }

    public void PlantSeed(CropData crop)
    {
        if (IsOccupied) return;

        currentCrop = crop;
        currentStage = 0;
        growthTimer = 0f;
        UpdateSprite();
    }

    public void Harvest()
    {
        if (currentCrop == null || currentStage < currentCrop.MaxStages - 1) return; 

        InventoryManager.Instance.Hotbar.AddItem(currentCrop.harvestItem, currentCrop.yieldAmount);

        currentCrop = null;
        spriteRenderer.sprite = null;
    }

    private void UpdateSprite()
    {
        spriteRenderer.sprite = currentCrop.growthSprites[currentStage];
    }
}